package id.my.selo.sekost.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** A foreground/background pair used by tonal chips, badges and status indicators. */
@Immutable
data class ToneColors(
    val content: Color,
    val container: Color,
    val onContainer: Color,
)

/**
 * Semantic color tokens. Feature code reads colors only from here (or MaterialTheme for
 * Material components, which are mapped from these same tokens in [toMaterialColorScheme]).
 */
@Immutable
data class SekostColors(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceSunken: Color,
    val outline: Color,
    val outlineStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val brand: Color,
    val onBrand: Color,
    val brandContainer: Color,
    val onBrandContainer: Color,
    val accent: Color,
    val onAccent: Color,
    val accentContainer: Color,
    val onAccentContainer: Color,
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val onHero: Color,
    val vacant: ToneColors,
    val occupied: ToneColors,
    val maintenance: ToneColors,
    val paid: ToneColors,
    val unpaid: ToneColors,
    val complaint: ToneColors,
    val danger: Color,
    val scrim: Color,
    val isDark: Boolean,
)

internal val LightSekostColors = SekostColors(
    background = Palette.Krem100,
    surface = Palette.Krem50,
    surfaceRaised = Color.White,
    surfaceSunken = Palette.Krem200,
    outline = Palette.Krem300,
    outlineStrong = Palette.Tinta300,
    textPrimary = Palette.Tinta900,
    textSecondary = Palette.Tinta500,
    textTertiary = Palette.Tinta400,
    brand = Palette.Jade700,
    onBrand = Palette.Krem50,
    brandContainer = Palette.Jade100,
    onBrandContainer = Palette.Jade900,
    accent = Palette.Genteng600,
    onAccent = Color.White,
    accentContainer = Palette.Genteng100,
    onAccentContainer = Palette.Genteng700,
    heroGradientStart = Palette.Jade700,
    heroGradientEnd = Palette.Jade900,
    onHero = Palette.Krem50,
    vacant = ToneColors(Palette.Langit600, Palette.Langit100, Palette.Langit600),
    occupied = ToneColors(Palette.Jade600, Palette.Jade100, Palette.Jade700),
    maintenance = ToneColors(Palette.Kunyit600, Palette.Kunyit100, Palette.Kunyit600),
    paid = ToneColors(Palette.Jade600, Palette.Jade100, Palette.Jade700),
    unpaid = ToneColors(Palette.Genteng600, Palette.Genteng100, Palette.Genteng700),
    complaint = ToneColors(Palette.Jambu600, Palette.Jambu100, Palette.Jambu600),
    danger = Palette.Jambu600,
    scrim = Color(0x99000000),
    isDark = false,
)

internal val DarkSekostColors = SekostColors(
    background = Palette.Malam950,
    surface = Palette.Malam900,
    surfaceRaised = Palette.Malam800,
    surfaceSunken = Palette.Malam950,
    outline = Palette.Malam700,
    outlineStrong = Palette.Malam600,
    textPrimary = Palette.Malam100,
    textSecondary = Palette.Malam300,
    textTertiary = Palette.Tinta500,
    brand = Palette.Jade300,
    onBrand = Palette.Jade950,
    brandContainer = Palette.Jade900Dim,
    onBrandContainer = Palette.Jade200,
    accent = Palette.Genteng300,
    onAccent = Palette.Genteng900,
    accentContainer = Palette.Genteng900,
    onAccentContainer = Palette.Genteng300,
    heroGradientStart = Palette.Jade800,
    heroGradientEnd = Palette.Jade950,
    onHero = Palette.Krem50,
    vacant = ToneColors(Palette.Langit300, Palette.Langit900, Palette.Langit300),
    occupied = ToneColors(Palette.Jade300, Palette.Jade900Dim, Palette.Jade200),
    maintenance = ToneColors(Palette.Kunyit300, Palette.Kunyit900, Palette.Kunyit300),
    paid = ToneColors(Palette.Jade300, Palette.Jade900Dim, Palette.Jade200),
    unpaid = ToneColors(Palette.Genteng300, Palette.Genteng900, Palette.Genteng300),
    complaint = ToneColors(Palette.Jambu300, Palette.Jambu900, Palette.Jambu300),
    danger = Palette.Jambu300,
    scrim = Color(0xB3000000),
    isDark = true,
)

internal fun SekostColors.toMaterialColorScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = brand,
        onPrimary = onBrand,
        primaryContainer = brandContainer,
        onPrimaryContainer = onBrandContainer,
        secondary = accent,
        onSecondary = onAccent,
        secondaryContainer = accentContainer,
        onSecondaryContainer = onAccentContainer,
        tertiary = vacant.content,
        background = background,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceSunken,
        onSurfaceVariant = textSecondary,
        surfaceContainerLowest = surfaceRaised,
        surfaceContainerLow = surface,
        surfaceContainer = surfaceRaised,
        surfaceContainerHigh = surfaceRaised,
        surfaceContainerHighest = surfaceSunken,
        surfaceBright = surfaceRaised,
        surfaceDim = surfaceSunken,
        outline = outlineStrong,
        outlineVariant = outline,
        error = danger,
        scrim = scrim,
    )
}

val LocalSekostColors = staticCompositionLocalOf { LightSekostColors }
