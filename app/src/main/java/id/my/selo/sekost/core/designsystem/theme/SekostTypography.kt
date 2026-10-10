package id.my.selo.sekost.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import id.my.selo.sekost.R

@OptIn(ExperimentalTextApi::class)
private fun jakarta(weight: FontWeight) = Font(
    resId = R.font.plus_jakarta_sans,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

@OptIn(ExperimentalTextApi::class)
private fun fraunces(weight: FontWeight) = Font(
    resId = R.font.fraunces,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.Setting("opsz", 72f),
        FontVariation.Setting("SOFT", 50f),
    ),
)

/** UI sans — Plus Jakarta Sans, a typeface commissioned for the city of Jakarta. */
val JakartaSans = FontFamily(
    jakarta(FontWeight.Normal),
    jakarta(FontWeight.Medium),
    jakarta(FontWeight.SemiBold),
    jakarta(FontWeight.Bold),
    jakarta(FontWeight.ExtraBold),
)

/** Display serif — Fraunces, a soft "old style" serif used sparingly for headlines and numbers. */
val Fraunces = FontFamily(
    fraunces(FontWeight.Normal),
    fraunces(FontWeight.Medium),
    fraunces(FontWeight.SemiBold),
)

/**
 * Extra brand text styles that Material's [Typography] has no slot for.
 * Access via `SekostTheme.typography`.
 */
@Immutable
data class SekostTextStyles(
    val hero: TextStyle,
    val amount: TextStyle,
    val amountSmall: TextStyle,
    val overline: TextStyle,
)

internal val SekostTextStylesDefault = SekostTextStyles(
    hero = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.02).em,
    ),
    amount = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.01).em,
    ),
    amountSmall = TextStyle(fontFamily = JakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    overline = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.12.em,
    ),
)

internal val SekostMaterialTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.02).em,
    ),
    displayMedium = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 42.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.02).em,
    ),
    displaySmall = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.01).em,
    ),
    headlineLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.01).em,
    ),
    headlineMedium = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.01).em,
    ),
    titleMedium = TextStyle(fontFamily = JakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = JakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = JakartaSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = JakartaSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = JakartaSans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = JakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = JakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = JakartaSans, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

val LocalSekostTextStyles = staticCompositionLocalOf { SekostTextStylesDefault }
