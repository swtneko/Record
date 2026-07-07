package com.neko.record.service

import android.app.Service
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.IntentCompat
import com.neko.record.data.model.QualityTier
import com.neko.record.data.model.Resolution
import com.neko.record.data.repository.ScreenCaptureRepository
import com.neko.record.domain.DeviceDisplayMetrics
import com.neko.record.domain.ResolutionCalculator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground service owning the MediaProjection + VirtualDisplay + ImageReader
 * capture pipeline.
 *
 * Milestone 2 scope: prove frames are being captured at the device's *true*
 * resolution/aspect ratio (see [ResolutionCalculator]) and expose that as
 * observable state via [ScreenCaptureRepository]. Feeding captured frames into
 * an encoder + RTMP muxer is the RTMP Engine milestone's job — this service
 * currently just acquires each [android.media.Image] to prove the pipeline is
 * alive, records a frame count, and immediately closes it (no encoding yet).
 *
 * Pause/Resume (spec section 7) is intentionally **not** implemented here —
 * it needs the RTMP socket/encoder to exist first, so it lands in the
 * dedicated Pause Livestream milestone instead of being bolted on early.
 */
@AndroidEntryPoint
class ScreenCaptureService : Service() {

    @Inject
    lateinit var repository: ScreenCaptureRepository

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            Log.i(TAG, "MediaProjection stopped by the system or the user")
            stopCapture()
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ScreenCaptureNotifications.ACTION_STOP) {
            stopCapture()
            stopSelf()
            return START_NOT_STICKY
        }

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        val data = intent?.let { IntentCompat.getParcelableExtra(it, EXTRA_RESULT_DATA, Intent::class.java) }
        val realWidth = intent?.getIntExtra(EXTRA_REAL_WIDTH, 0) ?: 0
        val realHeight = intent?.getIntExtra(EXTRA_REAL_HEIGHT, 0) ?: 0
        val densityDpi = intent?.getIntExtra(EXTRA_DENSITY_DPI, 0) ?: 0
        val qualityOrdinal = intent?.getIntExtra(EXTRA_QUALITY_TIER, QualityTier.Q_720P.ordinal)
            ?: QualityTier.Q_720P.ordinal

        if (data == null || realWidth <= 0 || realHeight <= 0) {
            Log.e(TAG, "Missing MediaProjection permission result or real resolution; stopping")
            repository.onError("Missing screen capture permission result")
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = ScreenCaptureNotifications.buildNotification(
            this,
            contentText = getString(com.neko.record.R.string.notification_capture_content)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                ScreenCaptureNotifications.NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(ScreenCaptureNotifications.NOTIFICATION_ID, notification)
        }

        startCapture(
            resultCode = resultCode,
            data = data,
            real = Resolution(realWidth, realHeight),
            densityDpi = densityDpi,
            tier = QualityTier.entries[qualityOrdinal]
        )

        return START_REDELIVER_INTENT
    }

    private fun startCapture(resultCode: Int, data: Intent, real: Resolution, densityDpi: Int, tier: QualityTier) {
        val target = ResolutionCalculator.computeCaptureResolution(real, tier)
        repository.onStarting(real = real, target = target)

        val projectionManager = getSystemService(MediaProjectionManager::class.java)
        val projection = projectionManager.getMediaProjection(resultCode, data)
        mediaProjection = projection
        projection.registerCallback(projectionCallback, null)

        val reader = ImageReader.newInstance(
            target.width,
            target.height,
            android.graphics.PixelFormat.RGBA_8888,
            2
        )
        imageReader = reader

        reader.setOnImageAvailableListener({ availableReader ->
            val image = availableReader.acquireLatestImage()
            if (image != null) {
                // Milestone 2: prove the pipeline delivers frames at the right
                // resolution. Encoding/muxing is added in the RTMP Engine milestone.
                repository.onFrameCaptured()
                image.close()
            }
        }, null)

        virtualDisplay = projection.createVirtualDisplay(
            VIRTUAL_DISPLAY_NAME,
            target.width,
            target.height,
            densityDpi.takeIf { it > 0 } ?: DEFAULT_DENSITY_DPI,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            null
        )
    }

    private fun stopCapture() {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
        mediaProjection?.unregisterCallback(projectionCallback)
        mediaProjection?.stop()
        mediaProjection = null
        repository.onStopped()
    }

    override fun onDestroy() {
        stopCapture()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ScreenCaptureService"
        private const val VIRTUAL_DISPLAY_NAME = "NekoRecordCapture"
        private const val DEFAULT_DENSITY_DPI = 320

        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
        const val EXTRA_REAL_WIDTH = "extra_real_width"
        const val EXTRA_REAL_HEIGHT = "extra_real_height"
        const val EXTRA_DENSITY_DPI = "extra_density_dpi"
        const val EXTRA_QUALITY_TIER = "extra_quality_tier"
    }
}
