package com.neko.record.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Top-level bottom navigation destinations. Each later milestone adds its own
 * nested graph under one of these routes rather than growing this file.
 */
sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object LiveStream : Screen(
        route = "live_stream",
        label = "Live Stream",
        selectedIcon = Icons.Filled.Videocam,
        unselectedIcon = Icons.Outlined.Videocam
    )

    data object ScreenRecord : Screen(
        route = "screen_record",
        label = "Screen Record",
        selectedIcon = Icons.Filled.PlayCircle,
        unselectedIcon = Icons.Outlined.PlayCircle
    )

    data object Tools : Screen(
        route = "tools",
        label = "Tools",
        selectedIcon = Icons.Outlined.Build,
        unselectedIcon = Icons.Outlined.Build
    )

    data object Videos : Screen(
        route = "videos",
        label = "Videos",
        selectedIcon = Icons.Outlined.PlayCircle,
        unselectedIcon = Icons.Outlined.PlayCircle
    )

    companion object {
        val bottomNavItems = listOf(LiveStream, ScreenRecord, Tools, Videos)
    }
}
