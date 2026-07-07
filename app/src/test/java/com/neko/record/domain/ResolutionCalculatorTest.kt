package com.neko.record.domain

import com.neko.record.data.model.QualityTier
import com.neko.record.data.model.Resolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolutionCalculatorTest {

    @Test
    fun `perfect quality on a 1080x2460 device returns exact device pixels`() {
        // Spec example: "1080x2460 thi encoder phai lay dung 1080x2460".
        val real = Resolution(1080, 2460)

        val result = ResolutionCalculator.computeCaptureResolution(real, QualityTier.Q_1080P)

        assertEquals(1080, result.width)
        assertEquals(2460, result.height)
    }

    @Test
    fun `720p on a 20 by 9 portrait device keeps the true 20by9 aspect, not 16by9`() {
        // 1080x2460 is ~20.5:9 — a naive 16:9 canvas would be 1080x1920.
        val real = Resolution(1080, 2460)

        val result = ResolutionCalculator.computeCaptureResolution(real, QualityTier.Q_720P)

        assertEquals(720, result.width)
        // Long side should scale by the same factor as the short side (720/1080),
        // landing near 1640, not the 16:9 value of 1280.
        val expectedLong = (2460.0 * (720.0 / 1080.0)).toInt()
        assertTrue("expected height near $expectedLong, was ${result.height}", kotlin.math.abs(result.height - expectedLong) <= 1)
        assertTrue("must not collapse to 16:9 (1280)", result.height != 1280)
    }

    @Test
    fun `landscape device stays landscape after scaling`() {
        val real = Resolution(2460, 1080)

        val result = ResolutionCalculator.computeCaptureResolution(real, QualityTier.Q_480P)

        assertTrue("width must remain >= height for a landscape source", result.width >= result.height)
        assertEquals(480, result.height)
    }

    @Test
    fun `tablet 4 by 3 aspect ratio is preserved, not forced to 16by9`() {
        val real = Resolution(1600, 1200) // 4:3 tablet, landscape

        val result = ResolutionCalculator.computeCaptureResolution(real, QualityTier.Q_720P)

        assertEquals(720, result.height)
        val expectedWidth = (1600.0 * (720.0 / 1200.0)).toInt()
        assertTrue(kotlin.math.abs(result.width - expectedWidth) <= 1)
    }

    @Test
    fun `never upscales beyond the real device resolution`() {
        val real = Resolution(720, 1600) // a real short side smaller than 1080/1440/2160 tiers

        val result4k = ResolutionCalculator.computeCaptureResolution(real, QualityTier.Q_4K)
        val result1080 = ResolutionCalculator.computeCaptureResolution(real, QualityTier.Q_1080P)

        assertEquals(real, result4k)
        assertEquals(real, result1080)
    }

    @Test
    fun `dimensions are always even for encoder compatibility`() {
        val real = Resolution(1081, 2461) // deliberately odd real dimensions

        val result = ResolutionCalculator.computeCaptureResolution(real, QualityTier.Q_720P)

        assertEquals(0, result.width % 2)
        assertEquals(0, result.height % 2)
    }
}
