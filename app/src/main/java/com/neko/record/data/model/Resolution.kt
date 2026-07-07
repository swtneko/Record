package com.neko.record.data.model

/**
 * A concrete pixel size. Always represents the *actual* capture/encode target —
 * never a hard-coded 9:16 or 16:9 canvas. See [com.neko.record.domain.ResolutionCalculator].
 */
data class Resolution(val width: Int, val height: Int) {
    val isLandscape: Boolean get() = width >= height

    /** Long side / short side, e.g. 2.05f for a 1080x2214 (≈19.5:9) panel. */
    val aspectRatio: Float get() = maxOf(width, height).toFloat() / minOf(width, height).toFloat()
}

/**
 * User-facing quality options from the spec (360p/480p/720p/1080p/2K/4K).
 * [shortSide] is the target length of the screen's *short* edge — the long edge
 * is derived from the device's real aspect ratio, so a 20:9 phone at "720p"
 * does not become a 16:9 or 9:16 canvas, just a scaled-down 20:9 canvas.
 */
enum class QualityTier(val label: String, val shortSide: Int) {
    Q_360P("360p", 360),
    Q_480P("480p", 480),
    Q_720P("720p", 720),
    Q_1080P("1080p", 1080),
    Q_2K("2K", 1440),
    Q_4K("4K", 2160);

    companion object {
        /** Only offer tiers the device can actually satisfy without upscaling. */
        fun availableFor(deviceShortSide: Int): List<QualityTier> =
            entries.filter { it.shortSide <= deviceShortSide }.ifEmpty { listOf(entries.first()) }
    }
}

/** Spec-required FPS choices: 24 / 25 / 30 / 50 / 60. */
enum class FpsOption(val value: Int) {
    FPS_24(24), FPS_25(25), FPS_30(30), FPS_50(50), FPS_60(60)
}
