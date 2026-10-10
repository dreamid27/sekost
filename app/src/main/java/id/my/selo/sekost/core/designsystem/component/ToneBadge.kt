package id.my.selo.sekost.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.designsystem.theme.ToneColors

/** Small tonal pill: status of a room, paid/unpaid, issue type… */
@Composable
fun ToneBadge(
    text: String,
    tone: ToneColors,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    showDot: Boolean = icon == null,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(SekostTheme.radii.pill))
            .background(tone.container)
            .padding(horizontal = SekostTheme.spacing.sm + SekostTheme.spacing.xxs, vertical = SekostTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xs + SekostTheme.spacing.xxs),
    ) {
        when {
            icon != null -> Icon(icon, null, tint = tone.content, modifier = Modifier.size(14.dp))
            showDot -> StatusDot(tone.content)
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = tone.onContainer, maxLines = 1)
    }
}

@Composable
fun StatusDot(color: Color, modifier: Modifier = Modifier) {
    Box(modifier.size(7.dp).clip(CircleShape).background(color))
}

/** Rounded square holding an icon, tinted by a [ToneColors]. */
@Composable
fun IconTile(
    icon: ImageVector,
    tone: ToneColors,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = SekostTheme.sizes.iconTile,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(SekostTheme.radii.md))
            .background(tone.container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tone.content, modifier = Modifier.size(size * 0.5f))
    }
}
