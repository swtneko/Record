package com.neko.record.ui.home

import com.neko.record.data.model.PlatformId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Milestone 1 baseline coverage: verifies the platform grid seeds correctly
 * and that all six required platforms from the spec are present exactly once.
 */
class HomeViewModelTest {

    @Test
    fun `initial state exposes six platforms`() {
        val viewModel = HomeViewModel()

        assertEquals(6, viewModel.uiState.value.platforms.size)
    }

    @Test
    fun `all required platform ids are present exactly once`() {
        val viewModel = HomeViewModel()
        val ids = viewModel.uiState.value.platforms.map { it.id }

        val expected = setOf(
            PlatformId.YOUTUBE,
            PlatformId.FACEBOOK,
            PlatformId.TWITCH,
            PlatformId.TIKTOK,
            PlatformId.TWITTER,
            PlatformId.CUSTOM_RTMP
        )

        assertEquals(expected, ids.toSet())
        assertTrue("no duplicate platform ids", ids.size == ids.toSet().size)
    }
}
