package id.my.selo.sekost.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import id.my.selo.sekost.R
import id.my.selo.sekost.core.common.displayName
import id.my.selo.sekost.core.designsystem.component.IconTile
import id.my.selo.sekost.core.designsystem.component.SekostCard
import id.my.selo.sekost.core.designsystem.component.SekostOutlinedButton
import id.my.selo.sekost.core.designsystem.component.ToneBadge
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.model.Issue
import id.my.selo.sekost.core.model.IssueStatus

/** A maintenance/complaint entry with a primary resolve/reopen action. */
@Composable
fun IssueCard(
    issue: Issue,
    onResolve: () -> Unit,
    onReopen: () -> Unit,
    modifier: Modifier = Modifier,
    context: String? = null,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    val colors = SekostTheme.colors
    val resolved = issue.status == IssueStatus.RESOLVED
    SekostCard(
        modifier = modifier.fillMaxWidth().testTag("issue_${issue.id}"),
        onClick = onClick,
        color = if (resolved) colors.surface else colors.surfaceRaised,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            IconTile(
                issue.type.icon,
                if (resolved) colors.occupied else issue.type.tone,
                size = SekostTheme.sizes.iconTile - SekostTheme.spacing.xs,
            )
            Spacer(Modifier.width(SekostTheme.spacing.md))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xxs)) {
                Text(
                    issue.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (resolved) colors.textSecondary else colors.textPrimary,
                    textDecoration = if (resolved) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (context != null) {
                    Text(
                        context,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.brand,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    if (resolved && issue.resolvedOn != null) {
                        stringResource(R.string.issue_resolved_on, issue.resolvedOn.displayName())
                    } else {
                        stringResource(R.string.issue_reported_on, issue.reportedOn.displayName())
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(SekostTheme.sizes.iconTile - SekostTheme.spacing.xs)) {
                    Icon(
                        SekostIcons.Trash,
                        stringResource(R.string.action_delete),
                        tint = colors.textTertiary,
                        modifier = Modifier.size(SekostTheme.sizes.iconSm + SekostTheme.spacing.xxs),
                    )
                }
            }
        }
        if (issue.description.isNotBlank()) {
            Text(
                issue.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = SekostTheme.spacing.sm),
            )
        }
        Row(Modifier.padding(top = SekostTheme.spacing.md), verticalAlignment = Alignment.CenterVertically) {
            ToneBadge(
                stringResource(issue.type.labelRes),
                issue.type.tone,
            )
            Spacer(Modifier.weight(1f))
            SekostOutlinedButton(
                text = stringResource(if (resolved) R.string.issue_reopen else R.string.issue_mark_resolved),
                onClick = if (resolved) onReopen else onResolve,
                icon = if (resolved) SekostIcons.Undo else SekostIcons.Check,
                contentColor = if (resolved) colors.textSecondary else colors.brand,
                modifier = Modifier.testTag("issue_toggle_${issue.id}"),
            )
        }
    }
}
