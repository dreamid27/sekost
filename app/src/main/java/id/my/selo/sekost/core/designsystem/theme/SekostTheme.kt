package id.my.selo.sekost.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Root theme. Wraps [MaterialTheme] so stock Material 3 components inherit the brand, and
 * provides Sekost-specific tokens (status colors, spacing, radii, …) via [SekostTheme].
 */
@Composable
fun SekostTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkSekostColors else LightSekostColors
    val radii = SekostRadii()
    CompositionLocalProvider(
        LocalSekostColors provides colors,
        LocalSekostTextStyles provides SekostTextStylesDefault,
        LocalSekostSpacing provides SekostSpacing(),
        LocalSekostRadii provides radii,
        LocalSekostSizes provides SekostSizes(),
        LocalSekostMotion provides SekostMotion(),
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography = SekostMaterialTypography,
            shapes = radii.toMaterialShapes(),
            content = content,
        )
    }
}

/** Single entry point for design tokens: `SekostTheme.colors.brand`, `SekostTheme.spacing.lg`, … */
object SekostTheme {
    val colors: SekostColors
        @Composable @ReadOnlyComposable
        get() = LocalSekostColors.current

    val typography: SekostTextStyles
        @Composable @ReadOnlyComposable
        get() = LocalSekostTextStyles.current

    val spacing: SekostSpacing
        @Composable @ReadOnlyComposable
        get() = LocalSekostSpacing.current

    val radii: SekostRadii
        @Composable @ReadOnlyComposable
        get() = LocalSekostRadii.current

    val sizes: SekostSizes
        @Composable @ReadOnlyComposable
        get() = LocalSekostSizes.current

    val motion: SekostMotion
        @Composable @ReadOnlyComposable
        get() = LocalSekostMotion.current
}
