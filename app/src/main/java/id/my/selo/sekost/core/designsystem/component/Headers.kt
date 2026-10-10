package id.my.selo.sekost.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.theme.SekostTheme

/**
 * Editorial-style top bar: optional back button row, then a serif title with an overline.
 * Lives inside the scrolling content so it scrolls away naturally.
 */
@Composable
fun SekostTopBar(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
    onBack: (() -> Unit)? = null,
    backLabel: String = "Kembali",
    horizontalPadding: Dp = SekostTheme.spacing.screenGutter,
    actions: @Composable () -> Unit = {},
) {
    Column(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = horizontalPadding)
            .padding(top = SekostTheme.spacing.sm, bottom = SekostTheme.spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = SekostTheme.colors.surfaceRaised),
                ) { Icon(SekostIcons.Back, contentDescription = backLabel, tint = SekostTheme.colors.textPrimary) }
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xs)) { actions() }
        }
        if (overline != null) {
            Text(
                overline.uppercase(),
                style = SekostTheme.typography.overline,
                color = SekostTheme.colors.accent,
                modifier = Modifier.padding(top = SekostTheme.spacing.sm),
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.headlineLarge,
            color = SekostTheme.colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = SekostTheme.spacing.xs),
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = SekostTheme.spacing.xl, bottom = SekostTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = SekostTheme.colors.textPrimary, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            SekostTextButton(text = actionLabel, onClick = onAction, icon = actionIcon)
        }
    }
}

@Composable
fun KeyValueRow(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = SekostTheme.colors.textPrimary) {
    Row(modifier.fillMaxWidth().padding(vertical = SekostTheme.spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = SekostTheme.colors.textSecondary, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(SekostTheme.spacing.md))
        Text(value, style = MaterialTheme.typography.titleSmall, color = valueColor)
    }
}
