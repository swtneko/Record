package com.neko.record.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NekoDarkScheme = darkColorScheme(
    primary = NekoAccent,
    onPrimary = NekoOnSurface,
    secondary = NekoAccentSoft,
    background = NekoBackground,
    onBackground = NekoOnSurface,
    surface = NekoSurface,
    onSurface = NekoOnSurface,
    surfaceVariant = NekoSurfaceElevated,
    onSurfaceVariant = NekoOnSurfaceMuted,
    outline = NekoOutline
)

// Milestone 1 ships dark-only, matching the reference app's default. A light
// scheme + user-facing theme toggle is planned for the Settings milestone.
private val NekoLightScheme = lightColorScheme(
    primary = NekoAccent,
    secondary = NekoAccentSoft
)

@Composable
fun NekoRecordTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NekoDarkScheme else NekoLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NekoTypography,
        content = content
    )
}
