package com.neko.record.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A destination platform the user can attach an outgoing RTMP session to.
 *
 * This is intentionally just a display/navigation model for Milestone 1.
 * Auth state, stream key storage, and the actual RTMP client binding are
 * introduced in the RTMP Engine milestone and will live in their own
 * `StreamTarget` domain model instead of being bolted onto this UI model.
 */
enum class PlatformId {
    YOUTUBE,
    FACEBOOK,
    TWITCH,
    TIKTOK,
    TWITTER,
    CUSTOM_RTMP
}

data class PlatformUiModel(
    val id: PlatformId,
    val label: String,
    val icon: ImageVector,
    val brandColor: Color
)
