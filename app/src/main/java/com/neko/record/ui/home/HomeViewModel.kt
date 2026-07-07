package com.neko.record.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.lifecycle.ViewModel
import com.neko.record.data.model.PlatformId
import com.neko.record.data.model.PlatformUiModel
import com.neko.record.ui.theme.BrandFacebook
import com.neko.record.ui.theme.BrandRtmp
import com.neko.record.ui.theme.BrandTikTok
import com.neko.record.ui.theme.BrandTwitch
import com.neko.record.ui.theme.BrandTwitter
import com.neko.record.ui.theme.BrandYouTube
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class HomeUiState(
    val platforms: List<PlatformUiModel> = emptyList()
)

/**
 * Milestone 1 scope: exposes the static platform grid only. Selecting a
 * platform will, starting with the RTMP Engine milestone, push a connect/login
 * flow (see images 4–5 of the reference: stream key entry, YouTube login).
 * For now `onPlatformSelected` is a no-op hook the Home screen already wires up
 * so later milestones only need to fill in the navigation call.
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(platforms = defaultPlatforms()))
    val uiState: StateFlow<HomeUiState> = _uiState

    fun onPlatformSelected(id: PlatformId) {
        // Intentionally empty in Milestone 1 — wired up in the RTMP Engine milestone.
    }

    private fun defaultPlatforms(): List<PlatformUiModel> = listOf(
        PlatformUiModel(PlatformId.YOUTUBE, "YouTube", Icons.Filled.LiveTv, BrandYouTube),
        PlatformUiModel(PlatformId.FACEBOOK, "Facebook", Icons.Filled.Facebook, BrandFacebook),
        PlatformUiModel(PlatformId.TWITCH, "Twitch", Icons.Filled.Videocam, BrandTwitch),
        PlatformUiModel(PlatformId.TIKTOK, "TikTok", Icons.Filled.MusicNote, BrandTikTok),
        PlatformUiModel(PlatformId.TWITTER, "Twitter / X", Icons.Filled.LiveTv, BrandTwitter),
        PlatformUiModel(PlatformId.CUSTOM_RTMP, "Custom RTMP", Icons.Filled.Cameraswitch, BrandRtmp)
    )
}
