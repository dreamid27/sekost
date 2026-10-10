package id.my.selo.sekost.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.designsystem.theme.ToneColors

/** One option of a [SegmentedPicker]. [tone] colors the selected state (defaults to brand). */
data class Segment<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null,
    val tone: ToneColors? = null,
)

/** Pill-shaped single choice control, used for room status, property type, theme mode… */
@Composable
fun <T> SegmentedPicker(
    segments: List<Segment<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SekostTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SekostTheme.radii.pill))
            .background(colors.surfaceSunken)
            .padding(SekostTheme.spacing.xs)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xs),
    ) {
        segments.forEach { segment ->
            val isSelected = segment.value == selected
            val tone = segment.tone ?: ToneColors(colors.brand, colors.surfaceRaised, colors.textPrimary)
            val container by animateColorAsState(
                if (isSelected) colors.surfaceRaised else Color.Transparent,
                tween(SekostTheme.motion.medium),
                label = "segment-bg",
            )
            val content by animateColorAsState(
                if (isSelected) tone.onContainer else colors.textSecondary,
                tween(SekostTheme.motion.medium),
                label = "segment-fg",
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(SekostTheme.sizes.chipHeight + SekostTheme.spacing.sm)
                    .clip(RoundedCornerShape(SekostTheme.radii.pill))
                    .background(container)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                SekostTheme.sizes.hairline,
                                colors.outline,
                                RoundedCornerShape(SekostTheme.radii.pill),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(segment.value) })
                    .padding(horizontal = SekostTheme.spacing.sm),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    segment.icon != null -> {
                        Icon(
                            segment.icon,
                            null,
                            tint = if (isSelected) tone.content else content,
                            modifier = Modifier.size(SekostTheme.sizes.iconSm),
                        )
                        Spacer(Modifier.width(SekostTheme.spacing.xs + SekostTheme.spacing.xxs))
                    }
                    segment.tone != null && isSelected -> {
                        StatusDot(tone.content)
                        Spacer(Modifier.width(SekostTheme.spacing.xs + SekostTheme.spacing.xxs))
                    }
                }
                Text(
                    segment.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
