package id.my.selo.sekost.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 4dp-based spacing scale. Use these instead of raw `dp` literals for padding and gaps. */
@Immutable
data class SekostSpacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val huge: Dp = 48.dp,
    /** Horizontal padding of every screen's content column. */
    val screenGutter: Dp = 20.dp,
)

/** Corner radius scale. Cards use [lg], sheets use [xl], chips/buttons use [pill]. */
@Immutable
data class SekostRadii(
    val xs: Dp = 6.dp,
    val sm: Dp = 10.dp,
    val md: Dp = 14.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 28.dp,
    val pill: Dp = 999.dp,
)

/** Fixed sizes for repeated elements. */
@Immutable
data class SekostSizes(
    val iconSm: Dp = 16.dp,
    val iconMd: Dp = 20.dp,
    val iconLg: Dp = 24.dp,
    val iconTile: Dp = 44.dp,
    val buttonHeight: Dp = 52.dp,
    val chipHeight: Dp = 32.dp,
    val hairline: Dp = 1.dp,
)

/** Motion durations in milliseconds. */
@Immutable
data class SekostMotion(
    val fast: Int = 150,
    val medium: Int = 250,
    val slow: Int = 400,
)

internal fun SekostRadii.toMaterialShapes() = Shapes(
    extraSmall = RoundedCornerShape(xs),
    small = RoundedCornerShape(sm),
    medium = RoundedCornerShape(md),
    large = RoundedCornerShape(lg),
    extraLarge = RoundedCornerShape(xl),
)

val LocalSekostSpacing = staticCompositionLocalOf { SekostSpacing() }
val LocalSekostRadii = staticCompositionLocalOf { SekostRadii() }
val LocalSekostSizes = staticCompositionLocalOf { SekostSizes() }
val LocalSekostMotion = staticCompositionLocalOf { SekostMotion() }
