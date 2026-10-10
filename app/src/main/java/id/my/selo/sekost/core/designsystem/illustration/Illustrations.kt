package id.my.selo.sekost.core.designsystem.illustration

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.my.selo.sekost.core.designsystem.theme.SekostColors
import id.my.selo.sekost.core.designsystem.theme.SekostTheme

/*
 * Sekost spot illustrations.
 *
 * Style rules: flat shapes, no outlines, one soft "blob" backdrop, terracotta roofs and jade
 * accents, small sparkles for delight. Everything is drawn on a 240×180 viewport and colored
 * from theme tokens so the art follows light/dark mode automatically.
 */

internal const val VIEW_W = 240f
internal const val VIEW_H = 180f

@Immutable
internal data class Ink(
    val blob: Color,
    val wall: Color,
    val wallShade: Color,
    val roof: Color,
    val roofShade: Color,
    val door: Color,
    val glass: Color,
    val leaf: Color,
    val leafLight: Color,
    val sun: Color,
    val ground: Color,
    val sparkle: Color,
    val berry: Color,
    val sky: Color,
)

internal fun SekostColors.ink() = Ink(
    blob = brandContainer,
    wall = surfaceRaised,
    wallShade = surfaceSunken,
    roof = accent,
    roofShade = onAccentContainer,
    door = brand,
    glass = vacant.container,
    leaf = occupied.content,
    leafLight = occupied.container,
    sun = maintenance.content,
    ground = outline,
    sparkle = maintenance.content,
    berry = complaint.content,
    sky = vacant.content,
)

@Composable
private fun Illustration(modifier: Modifier, width: Dp, draw: DrawScope.(Ink) -> Unit) {
    val ink = SekostTheme.colors.ink()
    Canvas(modifier.width(width).aspectRatio(VIEW_W / VIEW_H)) {
        val s = size.width / VIEW_W
        withTransform({ scale(s, s, pivot = Offset.Zero) }) { draw(ink) }
    }
}

/** A cosy house under the sun — welcome / no properties yet. */
@Composable
fun HouseIllustration(modifier: Modifier = Modifier, width: Dp = 220.dp) = Illustration(modifier, width) { ink ->
    blob(ink.blob)
    drawCircle(ink.sun.copy(alpha = 0.9f), radius = 14f, center = Offset(186f, 40f))
    ground(ink.ground)
    // chimney + body
    drawRect(ink.roofShade, Offset(146f, 46f), Size(12f, 30f))
    drawRoundRect(ink.wall, Offset(72f, 82f), Size(96f, 72f), CornerRadius(4f))
    drawRect(ink.wallShade, Offset(72f, 82f), Size(96f, 8f))
    // roof
    drawPath(path(60f to 88f, 120f to 40f, 180f to 88f), ink.roof)
    drawPath(path(60f to 88f, 120f to 40f, 124f to 44f, 68f to 90f), ink.roofShade.copy(alpha = 0.35f))
    // door with arch
    archDoor(ink.door, left = 108f, top = 112f, width = 24f, bottom = 154f)
    drawCircle(ink.sun, 1.8f, Offset(127f, 134f))
    // windows
    window(ink, 82f, 100f)
    window(ink, 142f, 100f)
    // plants
    leafBush(ink, 52f, 154f)
    leafBush(ink, 188f, 154f, mirror = true)
    sparkle(ink.sparkle, 40f, 54f, 6f)
    sparkle(ink.sparkle, 206f, 86f, 4f)
}

/** An open door with a hanging key — no rooms yet. */
@Composable
fun DoorIllustration(modifier: Modifier = Modifier, width: Dp = 200.dp) = Illustration(modifier, width) { ink ->
    blob(ink.blob)
    ground(ink.ground)
    // frame and dark interior
    drawRoundRect(ink.wall, Offset(84f, 34f), Size(72f, 120f), CornerRadius(6f))
    drawRoundRect(ink.door.copy(alpha = 0.18f), Offset(92f, 42f), Size(56f, 112f), CornerRadius(3f))
    // ajar door leaf
    drawPath(path(92f to 42f, 128f to 52f, 128f to 148f, 92f to 154f), ink.door)
    drawCircle(ink.sun, 2.4f, Offset(121f, 102f))
    // doormat
    drawRoundRect(ink.roof, Offset(82f, 154f), Size(76f, 8f), CornerRadius(4f))
    // hook + key on the wall
    drawLine(ink.roofShade, Offset(178f, 70f), Offset(178f, 82f), 2.5f, cap = StrokeCap.Round)
    drawCircle(ink.sun, 7f, Offset(178f, 90f), style = Stroke(3.5f))
    drawLine(ink.sun, Offset(178f, 97f), Offset(178f, 118f), 3.5f, cap = StrokeCap.Round)
    drawLine(ink.sun, Offset(178f, 110f), Offset(184f, 110f), 3f, cap = StrokeCap.Round)
    drawLine(ink.sun, Offset(178f, 116f), Offset(183f, 116f), 3f, cap = StrokeCap.Round)
    // tag
    rotate(-12f, Offset(64f, 92f)) {
        drawRoundRect(ink.roof, Offset(50f, 82f), Size(30f, 18f), CornerRadius(4f))
        drawCircle(ink.wall, 2.4f, Offset(56f, 91f))
    }
    leafBush(ink, 196f, 154f, mirror = true)
    sparkle(ink.sparkle, 60f, 48f, 5f)
    sparkle(ink.sparkle, 198f, 46f, 7f)
}

/** Wallet, coins and a check — every bill is paid. */
@Composable
fun PaidIllustration(modifier: Modifier = Modifier, width: Dp = 200.dp) = Illustration(modifier, width) { ink ->
    blob(ink.blob)
    // coin stack
    repeat(4) { i ->
        val y = 132f - i * 10f
        drawOval(ink.roofShade, Offset(150f, y + 3f), Size(44f, 16f))
        drawOval(ink.sun, Offset(150f, y), Size(44f, 16f))
    }
    // wallet
    drawRoundRect(ink.door, Offset(58f, 78f), Size(104f, 72f), CornerRadius(14f))
    drawRoundRect(ink.leaf.copy(alpha = 0.55f), Offset(58f, 70f), Size(92f, 18f), CornerRadius(9f))
    drawRoundRect(ink.wall.copy(alpha = 0.22f), Offset(122f, 102f), Size(40f, 24f), CornerRadius(12f))
    drawCircle(ink.sun, 4f, Offset(138f, 114f))
    // check badge
    drawCircle(ink.wall, 22f, Offset(74f, 64f))
    drawCircle(ink.leaf, 18f, Offset(74f, 64f))
    val check = Path().apply {
        moveTo(65f, 64f)
        lineTo(71.5f, 70.5f)
        lineTo(83f, 58f)
    }
    drawPath(check, ink.wall, style = Stroke(4.5f, cap = StrokeCap.Round))
    sparkle(ink.sparkle, 192f, 52f, 7f)
    sparkle(ink.sparkle, 44f, 120f, 5f)
    sparkle(ink.sparkle, 120f, 40f, 4f)
}

/** A thriving potted plant — nothing needs attention. */
@Composable
fun CalmIllustration(modifier: Modifier = Modifier, width: Dp = 200.dp) = Illustration(modifier, width) { ink ->
    blob(ink.blob)
    ground(ink.ground)
    // leaves
    leaf(ink.leaf, Offset(120f, 112f), 40f, -38f)
    leaf(ink.leaf, Offset(120f, 112f), 46f, 0f)
    leaf(ink.leaf, Offset(120f, 112f), 40f, 38f)
    leaf(ink.leafLight, Offset(120f, 116f), 30f, -70f)
    leaf(ink.leafLight, Offset(120f, 116f), 30f, 70f)
    // pot
    drawPath(path(96f to 112f, 144f to 112f, 138f to 154f, 102f to 154f), ink.roof)
    drawRoundRect(ink.roofShade, Offset(92f, 106f), Size(56f, 10f), CornerRadius(4f))
    // tea cup
    drawRoundRect(ink.wall, Offset(162f, 136f), Size(24f, 18f), CornerRadius(6f))
    drawCircle(ink.wall, 6f, Offset(188f, 144f), style = Stroke(3f))
    drawLine(ink.ground, Offset(170f, 126f), Offset(170f, 120f), 2f, cap = StrokeCap.Round)
    drawLine(ink.ground, Offset(178f, 128f), Offset(178f, 120f), 2f, cap = StrokeCap.Round)
    sparkle(ink.sparkle, 70f, 56f, 6f)
    sparkle(ink.sparkle, 176f, 60f, 4f)
}

/** Sekost brand mark: terracotta roof over a jade door. */
@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val colors = SekostTheme.colors
    Canvas(modifier.width(size).aspectRatio(1f)) {
        val s = this.size.width / 48f
        withTransform({ scale(s, s, pivot = Offset.Zero) }) {
            drawRoundRect(colors.brand, Offset.Zero, Size(48f, 48f), CornerRadius(14f))
            val roof = Path().apply {
                moveTo(11f, 22f)
                lineTo(24f, 11f)
                lineTo(37f, 22f)
            }
            drawPath(roof, colors.accent, style = Stroke(4.5f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
            drawPath(archPath(18f, 24f, 12f, 38f), colors.onBrand)
            drawCircle(colors.accent, 1.6f, Offset(27f, 31.5f))
        }
    }
}

// ——— drawing helpers ———

internal fun DrawScope.blob(color: Color) {
    val p = Path().apply {
        moveTo(40f, 100f)
        cubicTo(30f, 50f, 90f, 18f, 140f, 24f)
        cubicTo(196f, 30f, 226f, 72f, 210f, 118f)
        cubicTo(198f, 156f, 150f, 170f, 110f, 166f)
        cubicTo(64f, 162f, 46f, 136f, 40f, 100f)
        close()
    }
    drawPath(p, color)
}

internal fun DrawScope.ground(color: Color) =
    drawLine(color, Offset(34f, 155f), Offset(206f, 155f), 3f, cap = StrokeCap.Round)

internal fun path(vararg points: Pair<Float, Float>) = Path().apply {
    points.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x, y) else lineTo(x, y) }
    close()
}

internal fun archPath(left: Float, top: Float, width: Float, bottom: Float) = Path().apply {
    val r = width / 2
    moveTo(left, bottom)
    lineTo(left, top + r)
    arcTo(Rect(left, top, left + width, top + width), 180f, 180f, false)
    lineTo(left + width, bottom)
    close()
}

internal fun DrawScope.archDoor(color: Color, left: Float, top: Float, width: Float, bottom: Float) =
    drawPath(archPath(left, top, width, bottom), color)

internal fun DrawScope.window(ink: Ink, left: Float, top: Float) {
    drawRoundRect(ink.glass, Offset(left, top), Size(18f, 20f), CornerRadius(3f))
    drawLine(ink.wall, Offset(left + 9f, top), Offset(left + 9f, top + 20f), 2f)
    drawLine(ink.wall, Offset(left, top + 10f), Offset(left + 18f, top + 10f), 2f)
}

internal fun DrawScope.leaf(color: Color, base: Offset, length: Float, angle: Float) {
    rotate(angle, base) {
        val p = Path().apply {
            moveTo(base.x, base.y)
            quadraticTo(base.x - length * 0.42f, base.y - length * 0.55f, base.x, base.y - length)
            quadraticTo(base.x + length * 0.42f, base.y - length * 0.55f, base.x, base.y)
            close()
        }
        drawPath(p, color)
    }
}

internal fun DrawScope.leafBush(ink: Ink, x: Float, groundY: Float, mirror: Boolean = false) {
    val dir = if (mirror) -1f else 1f
    leaf(ink.leaf, Offset(x, groundY), 30f, -20f * dir)
    leaf(ink.leafLight, Offset(x, groundY), 22f, 28f * dir)
    leaf(ink.leaf, Offset(x, groundY), 18f, -58f * dir)
}

internal fun DrawScope.sparkle(color: Color, cx: Float, cy: Float, r: Float) {
    val p = Path().apply {
        moveTo(cx, cy - r)
        quadraticTo(cx, cy, cx + r, cy)
        quadraticTo(cx, cy, cx, cy + r)
        quadraticTo(cx, cy, cx - r, cy)
        quadraticTo(cx, cy, cx, cy - r)
        close()
    }
    drawPath(p, color)
}
