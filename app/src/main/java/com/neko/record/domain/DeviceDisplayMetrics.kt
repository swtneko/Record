package com.neko.record.domain

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.neko.record.data.model.Resolution

/**
 * Reads the device's *real* screen resolution — full physical pixels, not the
 * app window's available area — so Milestone 2's capture pipeline reflects the
 * spec requirement of exact pixels (e.g. 1080x2460, 20:9, 4:3 tablets, folds)
 * with no assumption of 9:16 or 16:9.
 *
 * Uses the modern per-Activity WindowMetrics API on API 30+ (the pre-30
 * `Display.getRealMetrics` path is deprecated but kept as the minSdk-29
 * fallback since this project's minSdk is 29).
 */
object DeviceDisplayMetrics {

    fun getRealScreenResolution(activity: Activity): Resolution {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = activity.windowManager.currentWindowMetrics.bounds
            Resolution(bounds.width(), bounds.height())
        } else {
            @Suppress("DEPRECATION")
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.getRealMetrics(metrics)
            Resolution(metrics.widthPixels, metrics.heightPixels)
        }
    }

    fun getScreenDensityDpi(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.resources.displayMetrics.densityDpi
        } else {
            @Suppress("DEPRECATION")
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)
            metrics.densityDpi
        }
    }
}
