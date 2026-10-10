package id.my.selo.sekost.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.my.selo.sekost.core.designsystem.theme.SekostTheme

/** Segmented donut showing room status split. Segments are drawn in the given order. */
@Composable
fun StatusRing(
    segments: List<Pair<Float, Color>>,
    trackColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    strokeWidth: Dp = 10.dp,
    center: @Composable () -> Unit = {},
) {
    val duration = SekostTheme.motion.slow * 2
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(duration)) }
    val progress = reveal.value
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            val total = segments.sumOf { it.first.toDouble() }.toFloat()
            if (total <= 0f) return@Canvas
            val gap = if (segments.count { it.first > 0f } > 1) 6f else 0f
            var start = -90f
            segments.filter { it.first > 0f }.forEach { (value, color) ->
                val sweep = value / total * 360f * progress
                drawArc(
                    color,
                    start + gap / 2,
                    (sweep - gap).coerceAtLeast(0.5f),
                    false,
                    topLeft,
                    arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
                start += sweep
            }
        }
        center()
    }
}

/** Thin rounded progress bar. */
@Composable
fun ProgressTrack(
    fraction: Float,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
) {
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(SekostTheme.motion.slow * 2), label = "track")
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(SekostTheme.radii.pill))
            .background(trackColor),
    ) {
        if (animated > 0f) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated)
                    .clip(RoundedCornerShape(SekostTheme.radii.pill))
                    .background(color),
            )
        }
    }
}
