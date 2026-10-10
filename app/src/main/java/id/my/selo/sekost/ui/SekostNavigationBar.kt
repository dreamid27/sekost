package id.my.selo.sekost.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import id.my.selo.sekost.core.designsystem.theme.SekostTheme

/** Bottom bar with an expanding pill on the active tab (icon + label), icon-only otherwise. */
@Composable
fun SekostNavigationBar(selected: TopLevelDestination?, onSelect: (TopLevelDestination) -> Unit) {
    val colors = SekostTheme.colors
    Column(Modifier.fillMaxWidth().background(colors.surface)) {
        HorizontalDivider(color = colors.outline, thickness = SekostTheme.sizes.hairline)
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = SekostTheme.spacing.md, vertical = SekostTheme.spacing.sm + SekostTheme.spacing.xxs)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopLevelDestination.entries.forEach { destination ->
                val isSelected = destination == selected
                val label = stringResource(destination.labelRes)
                val container by animateColorAsState(
                    if (isSelected) colors.brandContainer else Color.Transparent,
                    tween(SekostTheme.motion.medium),
                    label = "nav-bg",
                )
                val content by animateColorAsState(
                    if (isSelected) colors.onBrandContainer else colors.textTertiary,
                    tween(SekostTheme.motion.medium),
                    label = "nav-fg",
                )
                Row(
                    Modifier
                        .clip(RoundedCornerShape(SekostTheme.radii.pill))
                        .background(container)
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(destination) })
                        .semantics { contentDescription = label }
                        .testTag(destination.testTag)
                        .height(SekostTheme.sizes.buttonHeight - SekostTheme.spacing.xs)
                        .padding(horizontal = SekostTheme.spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        destination.icon,
                        contentDescription = null,
                        tint = content,
                        modifier = Modifier.size(SekostTheme.sizes.iconLg - SekostTheme.spacing.xxs),
                    )
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn(tween(SekostTheme.motion.medium)) + expandHorizontally(tween(SekostTheme.motion.medium)),
                        exit = fadeOut(tween(SekostTheme.motion.fast)) + shrinkHorizontally(tween(SekostTheme.motion.medium)),
                    ) {
                        Row {
                            Spacer(Modifier.width(SekostTheme.spacing.sm))
                            Text(label, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
