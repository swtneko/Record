package com.neko.record.ui.screenrecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neko.record.data.model.FpsOption
import com.neko.record.data.model.QualityTier
import com.neko.record.data.model.Resolution
import com.neko.record.data.repository.CaptureStatus
import com.neko.record.data.repository.ScreenCaptureRepository
import com.neko.record.data.repository.ScreenCaptureUiState
import com.neko.record.domain.ResolutionCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Purely local UI selections — never written to from the repository's flow. */
private data class LocalSelection(
    val realResolution: Resolution? = null,
    val selectedQuality: QualityTier = QualityTier.Q_720P,
    val selectedFps: FpsOption = FpsOption.FPS_24,
    val availableQualities: List<QualityTier> = QualityTier.entries,
    val targetResolutionPreview: Resolution? = null
)

data class ScreenRecordScreenState(
    val realResolution: Resolution? = null,
    val selectedQuality: QualityTier = QualityTier.Q_720P,
    val selectedFps: FpsOption = FpsOption.FPS_24,
    val availableQualities: List<QualityTier> = QualityTier.entries,
    val targetResolutionPreview: Resolution? = null,
    val capture: ScreenCaptureUiState = ScreenCaptureUiState()
)

/**
 * Combines two independent flows rather than one self-updating StateFlow:
 * [localSelection] holds only user-driven choices (quality/FPS/detected
 * resolution), [ScreenCaptureRepository.uiState] holds only capture-in-progress
 * state written by the service. Merging them into [uiState] avoids any
 * read-modify-write cycle on a single mutable flow.
 */
@HiltViewModel
class ScreenRecordViewModel @Inject constructor(
    private val repository: ScreenCaptureRepository
) : ViewModel() {

    private val localSelection = MutableStateFlow(LocalSelection())

    val uiState: StateFlow<ScreenRecordScreenState> = combine(
        localSelection,
        repository.uiState
    ) { local, capture ->
        ScreenRecordScreenState(
            realResolution = local.realResolution,
            selectedQuality = local.selectedQuality,
            selectedFps = local.selectedFps,
            availableQualities = local.availableQualities,
            targetResolutionPreview = local.targetResolutionPreview,
            capture = capture
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = ScreenRecordScreenState()
    )

    fun onRealResolutionDetected(real: Resolution) {
        val current = localSelection.value
        localSelection.value = current.copy(
            realResolution = real,
            availableQualities = QualityTier.availableFor(minOf(real.width, real.height)),
            targetResolutionPreview = ResolutionCalculator.computeCaptureResolution(real, current.selectedQuality)
        )
    }

    fun onQualitySelected(tier: QualityTier) {
        val current = localSelection.value
        val real = current.realResolution ?: return
        localSelection.value = current.copy(
            selectedQuality = tier,
            targetResolutionPreview = ResolutionCalculator.computeCaptureResolution(real, tier)
        )
    }

    fun onFpsSelected(fps: FpsOption) {
        localSelection.value = localSelection.value.copy(selectedFps = fps)
    }

    fun isCapturing(): Boolean {
        val status = uiState.value.capture.status
        return status == CaptureStatus.CAPTURING || status == CaptureStatus.STARTING
    }
}
