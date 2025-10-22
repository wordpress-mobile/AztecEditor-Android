package org.wordpress.aztec.placeholders

import androidx.compose.runtime.Composable
import org.wordpress.aztec.AztecAttributes

interface ComposePlaceholderAdapter : PlaceholderManager.PlaceholderAdapter {
    /**
     * Optional sizing hints for the manager.
     *
     * Usage:
     * - Return [SizingPolicy.MatchWidthWrapContentHeight] if your content should
     *   match the editor width and wrap to its intrinsic height. The manager will
     *   pre-measure once offscreen to obtain the final height and avoid flicker.
     * - Return [SizingPolicy.AspectRatio] for media with known aspect ratio. The
     *   manager calculates height = width * ratio without composition.
     * - Return [SizingPolicy.FixedHeightPx] for fixed-height embeds.
     * - Return [SizingPolicy.Unknown] to keep legacy behavior; the manager will
     *   call your existing [calculateHeight] implementation.
     */
    fun sizingPolicy(attrs: AztecAttributes): SizingPolicy = SizingPolicy.Unknown

    /**
     * Optional hook to compute a final height before first paint.
     *
     * When to use:
     * - Your content height depends on Compose measurement (e.g., text wrapping)
     *   and you want a single pass without interim sizes.
     *
     * How it works:
     * - Manager provides a [measurer] that composes your content offscreen at an
     *   exact width and returns its measured height in pixels.
     * - Return that value to have the placeholder sized correctly up-front.
     * - Return null to let the manager fall back to [sizingPolicy] or legacy
     *   [calculateHeight].
     *
     * Notes:
     * - Runs on the main thread. Do not perform long blocking work here.
     * - Keep the content passed to [measurer.measure] minimal (only what affects
     *   size) to make pre-measure cheap.
     */
    suspend fun preComposeMeasureHeight(
        attrs: AztecAttributes,
        widthPx: Int,
        measurer: PlaceholderMeasurer
    ): Int? = null

    /**
     * Optional spacing added after the placeholder, in pixels.
     *
     * This increases the reserved text-flow height while keeping the overlay
     * view at the content height, producing a visual margin below the embed
     * without an extra redraw.
     */
    fun bottomSpacingPx(attrs: AztecAttributes): Int = 0

    /** Abstraction to measure Compose content offscreen at an exact width. */
    interface PlaceholderMeasurer {
        suspend fun measure(content: @Composable () -> Unit, widthPx: Int): Int
    }

    /** Sizing policy hints used by the manager to choose a measurement path. */
    sealed interface SizingPolicy {
        object Unknown : SizingPolicy
        object MatchWidthWrapContentHeight : SizingPolicy
        data class AspectRatio(val ratio: Float) : SizingPolicy
        data class FixedHeightPx(val heightPx: Int) : SizingPolicy
    }

    /**
     * Insets for positioning the overlay view within the reserved text area.
     *
     * This affects only the overlay position/size, not the reserved text-flow
     * height. Use this to keep content away from edges (e.g., rounded corners)
     * or to eliminate bottom inset if it causes clipping.
     *
     * Defaults match legacy behavior (10 px on each side). Return zeros for
     * edge-to-edge rendering.
     */
    data class OverlayPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)

    fun overlayPaddingPx(attrs: AztecAttributes): OverlayPadding = OverlayPadding(10, 10, 10, 10)

    /**
     * Optional tiny positive adjustment added to the overlay height (pixels).
     *
     * Purpose: guard against 1 px rounding differences between pre-measure and
     * runtime composition that could otherwise clip the last row/baseline.
     * Leave at 0 unless you observe such edge cases.
     */
    fun contentHeightAdjustmentPx(attrs: AztecAttributes): Int = 0
    /**
     * Use this method to draw the placeholder using Jetpack Compose.
     * @param placeholderUuid the placeholder UUID
     * @param attrs aztec attributes of the view
     */
    @Composable
    fun Placeholder(placeholderUuid: String, attrs: AztecAttributes)
}
