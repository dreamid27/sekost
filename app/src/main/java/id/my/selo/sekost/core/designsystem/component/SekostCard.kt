package id.my.selo.sekost.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import id.my.selo.sekost.core.designsystem.theme.SekostTheme

/** The standard content container: raised surface, hairline border, `radii.lg` corners, no shadow. */
@Composable
fun SekostCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = SekostTheme.colors.surfaceRaised,
    border: Color? = SekostTheme.colors.outline,
    contentPadding: PaddingValues = PaddingValues(SekostTheme.spacing.lg),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(SekostTheme.radii.lg)
    val stroke = border?.let { BorderStroke(SekostTheme.sizes.hairline, it) }
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = color, border = stroke) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    } else {
        Surface(modifier = modifier, shape = shape, color = color, border = stroke) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    }
}
