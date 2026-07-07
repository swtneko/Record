package com.neko.record.ui.screenrecord

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neko.record.R
import com.neko.record.data.model.FpsOption
import com.neko.record.data.repository.CaptureStatus
import com.neko.record.domain.DeviceDisplayMetrics
import com.neko.record.service.ScreenCaptureNotifications
import com.neko.record.service.ScreenCaptureService

@Composable
fun ScreenRecordScreen(
    viewModel: ScreenRecordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (activity != null) {
            viewModel.onRealResolutionDetected(DeviceDisplayMetrics.getRealScreenResolution(activity))
        }
    }

    val projectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val real = uiState.realResolution
        if (result.resultCode == Activity.RESULT_OK && result.data != null && real != null) {
            val intent = Intent(context, ScreenCaptureService::class.java).apply {
                putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, result.data)
                putExtra(ScreenCaptureService.EXTRA_REAL_WIDTH, real.width)
                putExtra(ScreenCaptureService.EXTRA_REAL_HEIGHT, real.height)
                putExtra(ScreenCaptureService.EXTRA_DENSITY_DPI, DeviceDisplayMetrics.getScreenDensityDpi(context))
                putExtra(ScreenCaptureService.EXTRA_QUALITY_TIER, uiState.selectedQuality.ordinal)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* If denied, the foreground notification simply won't show — the service still runs. */ }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.screen_record_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        RealResolutionCard(
            realWidth = uiState.realResolution?.width,
            realHeight = uiState.realResolution?.height,
            targetWidth = uiState.targetResolutionPreview?.width,
            targetHeight = uiState.targetResolutionPreview?.height
        )

        Column {
            Text(
                stringResource(R.string.screen_record_quality),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.availableQualities) { tier ->
                    FilterChip(
                        selected = tier == uiState.selectedQuality,
                        onClick = { viewModel.onQualitySelected(tier) },
                        label = { Text(tier.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        Column {
            Text(
                stringResource(R.string.screen_record_fps),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(FpsOption.entries) { fps ->
                    FilterChip(
                        selected = fps == uiState.selectedFps,
                        onClick = { viewModel.onFpsSelected(fps) },
                        label = { Text("${fps.value}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        CaptureStatusCard(
            status = uiState.capture.status,
            frameCount = uiState.capture.frameCount,
            errorMessage = uiState.capture.errorMessage
        )

        val capturing = viewModel.isCapturing()
        Button(
            onClick = {
                if (capturing) {
                    context.startService(
                        Intent(context, ScreenCaptureService::class.java)
                            .setAction(ScreenCaptureNotifications.ACTION_STOP)
                    )
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                    val projectionManager = context.getSystemService(MediaProjectionManager::class.java)
                    projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(if (capturing) R.string.screen_record_stop else R.string.screen_record_start))
        }
    }
}

@Composable
private fun RealResolutionCard(realWidth: Int?, realHeight: Int?, targetWidth: Int?, targetHeight: Int?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.screen_record_true_resolution),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (realWidth != null && realHeight != null) {
                val ratio = maxOf(realWidth, realHeight).toFloat() / minOf(realWidth, realHeight).toFloat()
                "$realWidth x $realHeight  (${"%.2f".format(ratio)}:1)"
            } else {
                "Detecting…"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (targetWidth != null && targetHeight != null) {
            Text(
                text = "Will capture at: $targetWidth x $targetHeight",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CaptureStatusCard(status: CaptureStatus, frameCount: Long, errorMessage: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = when (status) {
                CaptureStatus.IDLE -> "Idle"
                CaptureStatus.STARTING -> "Starting…"
                CaptureStatus.CAPTURING -> stringResource(R.string.screen_record_frames_captured)
                CaptureStatus.STOPPED -> "Stopped"
                CaptureStatus.ERROR -> errorMessage ?: "Error"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (status == CaptureStatus.CAPTURING) {
            Text(
                text = "$frameCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
