package com.neko.record.data.repository

import com.neko.record.data.model.Resolution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class CaptureStatus { IDLE, STARTING, CAPTURING, STOPPED, ERROR }

data class ScreenCaptureUiState(
    val status: CaptureStatus = CaptureStatus.IDLE,
    val realResolution: Resolution? = null,
    val targetResolution: Resolution? = null,
    val frameCount: Long = 0L,
    val errorMessage: String? = null
)

/**
 * Process-wide source of truth for screen capture state.
 *
 * [ScreenCaptureService] (which cannot be directly observed by a Composable)
 * writes to this on the MediaProjection/ImageReader callback thread;
 * [com.neko.record.ui.screenrecord.ScreenRecordViewModel] reads from it as a
 * StateFlow. This keeps the actual MediaProjection/VirtualDisplay machinery
 * fully inside the service, where its lifecycle belongs.
 */
@Singleton
class ScreenCaptureRepository @Inject constructor() {

    private val _uiState = MutableStateFlow(ScreenCaptureUiState())
    val uiState: StateFlow<ScreenCaptureUiState> = _uiState

    fun onStarting(real: Resolution, target: Resolution) {
        _uiState.value = ScreenCaptureUiState(
            status = CaptureStatus.STARTING,
            realResolution = real,
            targetResolution = target
        )
    }

    fun onFrameCaptured() {
        val current = _uiState.value
        _uiState.value = current.copy(
            status = CaptureStatus.CAPTURING,
            frameCount = current.frameCount + 1
        )
    }

    fun onStopped() {
        _uiState.value = _uiState.value.copy(status = CaptureStatus.STOPPED, frameCount = 0L)
    }

    fun onError(message: String) {
        _uiState.value = _uiState.value.copy(status = CaptureStatus.ERROR, errorMessage = message)
    }
}
