package id.my.selo.sekost.core.designsystem.illustration

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * Animated onboarding scenes. Same style rules as the static spot illustrations (flat shapes on a
 * 240×180 viewport, token colors only), plus two motion layers:
 *  - `appear`: a one-shot 0→1 entrance played when the scene first composes;
 *  - `loop`: a slow, seamless 0→1 cycle for ambient motion (swaying leaves, twinkling sparkles).
 * Animated values are read inside the draw phase, so motion never triggers recomposition.
 */

private const val LOOP_MS = 6_000
private const val APPEAR_MS = 900
private const val TAU = (2 * PI).toFloat()

private class Motion(appear: State<Float>, loop: State<Float>) {
    val appear by appear
    val loop by loop

    /** A smooth wave in -1..1 that completes [cycles] periods per loop, shifted by [offset] (0..1). */
    fun wave(cycles: Int = 1, offset: Float = 0f) = sin((loop * cycles + offset) * TAU)

    /** Twinkle scale for sparkles: breathes between 0.55 and 1.15. */
    fun twinkle(offset: Float) = 0.85f + 0.3f * wave(2, offset)
}

@Composable
private fun AnimatedIllustration(
    modifier: Modifier,
    width: Dp,
    appearMs: Int = APPEAR_MS,
    draw: DrawScope.(Ink, Motion) -> Unit,
) {
    val ink = SekostTheme.colors.ink()
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(appearMs, easing = FastOutSlowInEasing)) }
    val loop = rememberInfiniteTransition(label = "illustration-loop").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(LOOP_MS, easing = LinearEasing)),
        label = "illustration-phase",
    )
    val motion = remember { Motion(appear.asState(), loop) }
    Canvas(modifier.width(width).aspectRatio(VIEW_W / VIEW_H)) {
        val s = size.width / VIEW_W
        withTransform({ scale(s, s, pivot = Offset.Zero) }) { draw(ink, motion) }
    }
}

/** Welcome: the sun rises over a cosy kost, its door swings open and the windows light up. */
@Composable
fun WelcomeIllustration(modifier: Modifier = Modifier, width: Dp = SekostTheme.sizes.onboardingArt) =
    AnimatedIllustration(modifier, width, appearMs = APPEAR_MS * 2) { ink, m ->
        val a = m.appear
        blob(ink.blob)
        // the sun rises during the entrance, then floats
        val sunY = lerp(96f, 40f, a) + 2.5f * m.wave()
        drawCircle(ink.sun.copy(alpha = 0.22f), radius = 22f + 2f * m.wave(2), center = Offset(184f, sunY))
        drawCircle(ink.sun, radius = 14f, center = Offset(184f, sunY))
        cloud(ink.wall, 58f + 10f * m.wave(), 50f, 1f)
        cloud(ink.wall.copy(alpha = 0.8f), 150f - 8f * m.wave(1, 0.3f), 30f, 0.7f)
        ground(ink.ground)
        translate(top = (1f - a) * 18f) {
            cottage(ink, doorOpen = a, glow = a)
        }
        leafBush(ink, 52f, 155f, sway = 4f * m.wave(1, 0.1f))
        leafBush(ink, 190f, 155f, mirror = true, sway = 4f * m.wave(1, 0.6f))
        sparkle(ink.sparkle, 36f, 82f, 6f * m.twinkle(0f) * a)
        sparkle(ink.sparkle, 212f, 96f, 4.5f * m.twinkle(0.4f) * a)
        sparkle(ink.sparkle, 96f, 26f, 4f * m.twinkle(0.7f) * a)
    }

/**
 * Profile step: a nameplate swings on the front door, showing the owner's [monogram].
 * Changing the monogram gives the plate a playful nudge.
 */
@Composable
fun NameplateIllustration(monogram: String, modifier: Modifier = Modifier, width: Dp = SekostTheme.sizes.onboardingArt) {
    val measurer = rememberTextMeasurer()
    val style = MaterialTheme.typography.headlineMedium
    val nudge = remember { Animatable(0f) }
    LaunchedEffect(monogram) {
        nudge.snapTo(1f)
        nudge.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow))
    }
    AnimatedIllustration(modifier, width) { ink, m ->
        blob(ink.blob)
        ground(ink.ground)
        // facade with an arched door
        drawRoundRect(ink.wall, Offset(70f, 44f), Size(100f, 111f), CornerRadius(6f))
        drawRect(ink.wallShade, Offset(70f, 44f), Size(100f, 8f))
        drawPath(path(60f to 50f, 120f to 18f, 180f to 50f), ink.roof)
        archDoor(ink.door, left = 98f, top = 96f, width = 44f, bottom = 155f)
        drawCircle(ink.sun, 2.6f, Offset(134f, 128f))
        drawRoundRect(ink.roof, Offset(92f, 152f), Size(56f, 6f), CornerRadius(3f))
        // the nameplate hangs from a nail and swings around it
        val nail = Offset(120f, 60f)
        val swing = 5f * m.wave() + 16f * nudge.value * sin(nudge.value * 9f)
        drawCircle(ink.roofShade, 2.4f, nail)
        rotate(swing, nail) {
            val plate = Offset(96f, 70f)
            val plateSize = Size(48f, 22f)
            drawLine(ink.roofShade, nail, Offset(plate.x + 8f, plate.y), 1.6f, cap = StrokeCap.Round)
            drawLine(ink.roofShade, nail, Offset(plate.x + plateSize.width - 8f, plate.y), 1.6f, cap = StrokeCap.Round)
            drawRoundRect(ink.roofShade, plate + Offset(0f, 2f), plateSize, CornerRadius(6f))
            drawRoundRect(ink.roof, plate, plateSize, CornerRadius(6f))
            centeredText(measurer, monogram, style, ink.wall, plate + Offset(plateSize.width / 2, plateSize.height / 2), 13f)
        }
        // potted plants by the door
        drawPath(path(44f to 140f, 64f to 140f, 61f to 155f, 47f to 155f), ink.roof)
        leafBush(ink, 54f, 140f, sway = 4f * m.wave(1, 0.2f))
        drawPath(path(176f to 140f, 196f to 140f, 193f to 155f, 179f to 155f), ink.roof)
        leafBush(ink, 186f, 140f, mirror = true, sway = 4f * m.wave(1, 0.7f))
        sparkle(ink.sparkle, 36f, 70f, 6f * m.twinkle(0f) * m.appear)
        sparkle(ink.sparkle, 206f, 56f, 5f * m.twinkle(0.5f) * m.appear)
    }
}

/**
 * Billing step: a monthly calendar whose highlight glides to the chosen [dueDay] (1–28, laid out
 * as four 7-day weeks), while coins keep dropping onto a stack.
 */
@Composable
fun DueDateIllustration(dueDay: Int, modifier: Modifier = Modifier, width: Dp = SekostTheme.sizes.onboardingArt) {
    val measurer = rememberTextMeasurer()
    val style = MaterialTheme.typography.labelLarge
    val index = (dueDay.coerceIn(1, 28) - 1)
    val springSpec = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
    val col by animateFloatAsState((index % 7).toFloat(), springSpec, label = "due-col")
    val row by animateFloatAsState((index / 7).toFloat(), springSpec, label = "due-row")
    AnimatedIllustration(modifier, width) { ink, m ->
        blob(ink.blob)
        // calendar page
        val card = Offset(44f, 34f)
        drawRoundRect(ink.wallShade, card + Offset(0f, 4f), Size(128f, 124f), CornerRadius(14f))
        drawRoundRect(ink.wall, card, Size(128f, 124f), CornerRadius(14f))
        drawRoundRect(ink.roof, card, Size(128f, 26f), CornerRadius(14f, 14f))
        drawRect(ink.roof, card + Offset(0f, 14f), Size(128f, 12f))
        listOf(76f, 140f).forEach { x ->
            drawCircle(ink.wall, 3.4f, Offset(x, card.y + 12f))
            drawLine(ink.roofShade, Offset(x, card.y - 6f), Offset(x, card.y + 12f), 3f, cap = StrokeCap.Round)
        }
        val cellW = 16f
        val origin = Offset(card.x + 16f, card.y + 40f)
        fun cell(c: Float, r: Float) = origin + Offset(c * cellW, r * 22f)
        for (i in 0 until 28) {
            val passed = i < index
            drawCircle(if (passed) ink.leafLight else ink.ground, 2.6f, cell((i % 7).toFloat(), (i / 7).toFloat()))
        }
        val at = cell(col, row)
        val pulse = 1f + 0.06f * m.wave(2)
        drawCircle(ink.door.copy(alpha = 0.18f), 14f * pulse, at)
        drawCircle(ink.door, 10.5f, at)
        centeredText(measurer, dueDay.toString(), style, ink.wall, at, 11f)
        // coin stack with a coin dropping in on every loop
        repeat(3) { i -> coin(ink, Offset(196f, 146f - i * 9f)) }
        val drop = (m.loop * 2f) % 1f
        val fall = (drop / 0.55f).coerceAtMost(1f)
        val y = lerp(24f, 119f, fall * fall)
        val alpha = if (drop < 0.85f) 1f else (1f - drop) / 0.15f
        coin(ink, Offset(196f, y), alpha)
        if (fall >= 1f) sparkle(ink.sparkle, 218f, 112f, 6f * (1f - (drop - 0.55f) / 0.45f))
        sparkle(ink.sparkle, 30f, 60f, 5f * m.twinkle(0.2f) * m.appear)
    }
}

/**
 * Appearance step: sun and moon morph into each other. [night] is 0 for light, 1 for dark and
 * anything in between for "follow the system".
 */
@Composable
fun DayNightIllustration(night: Float, modifier: Modifier = Modifier, width: Dp = SekostTheme.sizes.onboardingArt) {
    val n by animateFloatAsState(night, tween(SekostTheme.motion.slow * 2, easing = FastOutSlowInEasing), label = "night")
    AnimatedIllustration(modifier, width) { ink, m ->
        blob(ink.blob)
        val body = Offset(120f, 70f)
        val radius = 30f
        // sun rays fade out and spin slowly
        rotate(m.loop * 90f, body) {
            repeat(8) { i ->
                val angle = i * TAU / 8
                val dir = Offset(cos(angle), sin(angle))
                drawLine(
                    ink.sun.copy(alpha = 1f - n),
                    body + dir * (radius + 8f),
                    body + dir * (radius + 15f + 2f * m.wave(2, i / 8f)),
                    4f,
                    cap = StrokeCap.Round,
                )
            }
        }
        drawCircle(lerp(ink.sun, ink.wall, n), radius, body)
        // the crescent: a "bite" in blob color slides over the disc as night falls
        if (n > 0f) {
            val disc = Path().apply { addOval(androidx.compose.ui.geometry.Rect(body, radius)) }
            clipPath(disc) {
                drawCircle(ink.blob, radius * 0.92f, body + Offset(lerp(64f, 16f, n), lerp(-30f, -12f, n)))
            }
        }
        // stars twinkle in at night
        listOf(Triple(64f, 44f, 0f), Triple(176f, 34f, 0.3f), Triple(190f, 92f, 0.6f), Triple(52f, 98f, 0.8f)).forEach { (x, y, o) ->
            sparkle(ink.sparkle, x, y, 5f * n * m.twinkle(o))
        }
        // clouds drift away by day
        cloud(ink.wall.copy(alpha = 1f - n), 66f + 8f * m.wave(), 82f, 0.9f)
        cloud(ink.wall.copy(alpha = (1f - n) * 0.85f), 176f - 6f * m.wave(1, 0.5f), 62f, 0.7f)
        // a little house whose window glows at night
        ground(ink.ground)
        drawRoundRect(ink.wall, Offset(96f, 122f), Size(48f, 33f), CornerRadius(3f))
        drawPath(path(88f to 126f, 120f to 102f, 152f to 126f), ink.roof)
        drawRoundRect(lerp(ink.glass, ink.sun, n), Offset(104f, 130f), Size(12f, 12f), CornerRadius(2f))
        archDoor(ink.door, left = 124f, top = 132f, width = 12f, bottom = 155f)
        leafBush(ink, 74f, 155f, sway = 3f * m.wave(1, 0.3f))
        leafBush(ink, 168f, 155f, mirror = true, sway = 3f * m.wave(1, 0.8f))
    }
}

/** Ready step: confetti bursts over the kost and a check badge pops in. */
@Composable
fun CelebrationIllustration(modifier: Modifier = Modifier, width: Dp = SekostTheme.sizes.onboardingArt) =
    AnimatedIllustration(modifier, width, appearMs = APPEAR_MS * 3) { ink, m ->
        val a = m.appear
        blob(ink.blob)
        ground(ink.ground)
        cottage(ink, doorOpen = 1f, glow = 1f)
        leafBush(ink, 52f, 155f, sway = 4f * m.wave(1, 0.1f))
        leafBush(ink, 190f, 155f, mirror = true, sway = 4f * m.wave(1, 0.6f))
        // check badge pops with a little overshoot
        val pop = backOut((a * 3f - 0.3f).coerceIn(0f, 1f))
        withTransform({ scale(pop, pop, pivot = Offset(176f, 52f)) }) {
            drawCircle(ink.wall, 20f, Offset(176f, 52f))
            drawCircle(ink.leaf, 16f, Offset(176f, 52f))
            val check = Path().apply {
                moveTo(168f, 52f)
                lineTo(174f, 58f)
                lineTo(184.5f, 46.5f)
            }
            drawPath(check, ink.wall, style = Stroke(4f, cap = StrokeCap.Round))
        }
        confetti(ink, a, m)
        sparkle(ink.sparkle, 40f, 60f, 6f * m.twinkle(0f) * a)
        sparkle(ink.sparkle, 210f, 104f, 4.5f * m.twinkle(0.35f) * a)
        sparkle(ink.sparkle, 84f, 30f, 4f * m.twinkle(0.7f) * a)
    }

// ——— scene pieces ———

/** The kost from the welcome scene; [doorOpen] swings the door, [glow] lights the windows. */
private fun DrawScope.cottage(ink: Ink, doorOpen: Float, glow: Float) {
    drawRect(ink.roofShade, Offset(146f, 46f), Size(12f, 30f))
    drawRoundRect(ink.wall, Offset(72f, 82f), Size(96f, 73f), CornerRadius(4f))
    drawRect(ink.wallShade, Offset(72f, 82f), Size(96f, 8f))
    drawPath(path(60f to 88f, 120f to 40f, 180f to 88f), ink.roof)
    drawPath(path(60f to 88f, 120f to 40f, 124f to 44f, 68f to 90f), ink.roofShade.copy(alpha = 0.35f))
    // warm light behind the door, then the door leaf narrowing as it swings inward
    drawPath(archPath(108f, 112f, 24f, 155f), lerp(ink.door.copy(alpha = 0.25f), ink.sun.copy(alpha = 0.55f), glow))
    val leafWidth = lerp(24f, 9f, doorOpen)
    drawPath(path(108f to 124f, 108f + leafWidth to 124f + 4f * doorOpen, 108f + leafWidth to 155f, 108f to 155f), ink.door)
    drawPath(archPath(108f, 112f, 24f, 124f + 12f), ink.door.copy(alpha = 1f - doorOpen))
    listOf(82f, 140f).forEach { x ->
        drawRoundRect(lerp(ink.glass, ink.sun, glow * 0.75f), Offset(x, 100f), Size(18f, 20f), CornerRadius(3f))
        drawLine(ink.wall, Offset(x + 9f, 100f), Offset(x + 9f, 120f), 2f)
        drawLine(ink.wall, Offset(x, 110f), Offset(x + 18f, 110f), 2f)
    }
}

private fun DrawScope.leafBush(ink: Ink, x: Float, groundY: Float, mirror: Boolean = false, sway: Float) {
    rotate(sway, Offset(x, groundY)) { leafBush(ink, x, groundY, mirror) }
}

private fun DrawScope.cloud(color: Color, x: Float, y: Float, scale: Float) {
    if (color.alpha <= 0f) return
    withTransform({ scale(scale, scale, pivot = Offset(x, y)) }) {
        drawRoundRect(color, Offset(x - 22f, y - 6f), Size(44f, 14f), CornerRadius(7f))
        drawCircle(color, 10f, Offset(x - 6f, y - 6f))
        drawCircle(color, 7f, Offset(x + 8f, y - 4f))
    }
}

/** A coin seen from the side; [topCenter] is the middle of its top edge. */
private fun DrawScope.coin(ink: Ink, topCenter: Offset, alpha: Float = 1f) {
    val topLeft = topCenter - Offset(18f, 0f)
    drawOval(ink.roofShade.copy(alpha = alpha), topLeft + Offset(0f, 3f), Size(36f, 13f))
    drawOval(ink.sun.copy(alpha = alpha), topLeft, Size(36f, 13f))
}

/** 22 deterministic particles that burst out over the roof, tumble down and fade away. */
private fun DrawScope.confetti(ink: Ink, appear: Float, m: Motion) {
    if (appear >= 1f) return
    val colors = listOf(ink.roof, ink.door, ink.sun, ink.leaf, ink.berry, ink.sky)
    val origin = Offset(120f, 70f)
    val t = appear
    val alpha = if (t < 0.7f) 1f else (1f - t) / 0.3f
    repeat(22) { i ->
        val angle = (-PI / 2 + (i - 10.5) * 0.14).toFloat()
        val speed = 70f + (i * 37 % 50)
        val pos = origin + Offset(cos(angle), sin(angle)) * speed * t + Offset(3f * m.wave(1, i / 22f), 70f * t * t)
        val color = colors[i % colors.size].copy(alpha = alpha)
        rotate(i * 47f + t * 540f, pos) {
            if (i % 3 == 0) {
                drawCircle(color, 2.6f, pos)
            } else {
                drawRoundRect(color, pos - Offset(4f, 1.6f), Size(8f, 3.2f), CornerRadius(1.6f))
            }
        }
    }
}

private fun DrawScope.centeredText(measurer: TextMeasurer, text: String, style: TextStyle, color: Color, center: Offset, sizeUnits: Float) {
    if (text.isEmpty()) return
    val layout = measurer.measure(text, style.copy(fontSize = sizeUnits.toSp(), color = color), maxLines = 1)
    drawText(layout, topLeft = center - Offset(layout.size.width / 2f, layout.size.height / 2f))
}

private fun lerp(start: Float, stop: Float, fraction: Float) = start + (stop - start) * fraction

/** Ease that overshoots slightly before settling — used for "pop" entrances. */
private fun backOut(t: Float): Float {
    val c = 1.9f
    val x = t - 1f
    return 1f + (c + 1f) * x * x * x + c * x * x
}
