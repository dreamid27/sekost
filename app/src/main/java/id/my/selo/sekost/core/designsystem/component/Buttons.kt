package id.my.selo.sekost.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import id.my.selo.sekost.core.designsystem.theme.SekostTheme

enum class ButtonTone { Brand, Accent, Danger }

@Composable
fun SekostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tone: ButtonTone = ButtonTone.Brand,
    enabled: Boolean = true,
) {
    val colors = SekostTheme.colors
    val (container, content) = when (tone) {
        ButtonTone.Brand -> colors.brand to colors.onBrand
        ButtonTone.Accent -> colors.accent to colors.onAccent
        ButtonTone.Danger -> colors.danger to colors.surfaceRaised
    }
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = SekostTheme.sizes.buttonHeight),
        enabled = enabled,
        shape = RoundedCornerShape(SekostTheme.radii.pill),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = colors.surfaceSunken,
            disabledContentColor = colors.textTertiary,
        ),
        contentPadding = PaddingValues(horizontal = SekostTheme.spacing.xxl, vertical = SekostTheme.spacing.md),
    ) { ButtonContent(text, icon) }
}

@Composable
fun SekostOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    contentColor: Color = SekostTheme.colors.textPrimary,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = SekostTheme.sizes.buttonHeight),
        enabled = enabled,
        shape = RoundedCornerShape(SekostTheme.radii.pill),
        border = BorderStroke(SekostTheme.sizes.hairline, SekostTheme.colors.outlineStrong),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        contentPadding = PaddingValues(horizontal = SekostTheme.spacing.xl, vertical = SekostTheme.spacing.md),
    ) { ButtonContent(text, icon) }
}

@Composable
fun SekostTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    contentColor: Color = SekostTheme.colors.brand,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(SekostTheme.radii.pill),
        colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
    ) { ButtonContent(text, icon) }
}

@Composable
private fun ButtonContent(text: String, icon: ImageVector?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(SekostTheme.sizes.iconMd))
            Spacer(Modifier.width(SekostTheme.spacing.sm))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
