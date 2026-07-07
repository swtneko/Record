package com.neko.record.domain

import com.neko.record.data.model.QualityTier
import com.neko.record.data.model.Resolution

/**
 * Computes the exact capture/encode resolution for a given real device screen
 * and a requested quality tier — **never** forcing 9:16 or 16:9.
 *
 * Spec requirement: "1080x2460 thì encoder phải lấy đúng 1080x2460, không crop,
 * không stretch, không ép về 9:16." This function is the single place that
 * decides pixel dimensions, so every caller (preview, VirtualDisplay, future
 * encoder) stays consistent with the device's true aspect ratio.
 *
 * Scaling rule: the short side is scaled to the requested [QualityTier.shortSide]
 * (or kept at the real short side if the tier would upscale), the long side is
 * derived from the device's real aspect ratio, and both dimensions are rounded
 * to the nearest even number (required by most hardware video encoders, which
 * reject odd width/height).
 */
object ResolutionCalculator {

    fun computeCaptureResolution(real: Resolution, tier: QualityTier): Resolution {
        require(real.width > 0 && real.height > 0) { "Real resolution must be positive: $real" }

        val realShort = minOf(real.width, real.height)
        val realLong = maxOf(real.width, real.height)

        // Never upscale beyond the device's actual pixels.
        val targetShort = minOf(tier.shortSide, realShort)

        val scale = targetShort.toDouble() / realShort.toDouble()
        val targetLong = (realLong * scale).roundToEven()
        val targetShortEven = targetShort.roundToEven()

        // Re-attach the scaled dimensions to whichever original axis was long/short,
        // so a landscape device (width >= height) stays landscape, and portrait
        // stays portrait — orientation is never flipped or forced here.
        return if (real.isLandscape) {
            Resolution(width = targetLong, height = targetShortEven)
        } else {
            Resolution(width = targetShortEven, height = targetLong)
        }
    }

    private fun Int.roundToEven(): Int {
        val rounded = this
        return if (rounded % 2 == 0) rounded else rounded - 1
    }

    private fun Double.roundToEven(): Int {
        val rounded = Math.round(this).toInt()
        return if (rounded % 2 == 0) rounded else rounded - 1
    }
}
