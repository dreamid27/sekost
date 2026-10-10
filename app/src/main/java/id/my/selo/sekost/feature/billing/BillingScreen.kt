package id.my.selo.sekost.feature.billing

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.selo.sekost.R
import id.my.selo.sekost.core.common.Rupiah
import id.my.selo.sekost.core.common.displayName
import id.my.selo.sekost.core.designsystem.component.EmptyState
import id.my.selo.sekost.core.designsystem.component.ProgressTrack
import id.my.selo.sekost.core.designsystem.component.Segment
import id.my.selo.sekost.core.designsystem.component.SegmentedPicker
import id.my.selo.sekost.core.designsystem.component.SekostCard
import id.my.selo.sekost.core.designsystem.component.SekostOutlinedButton
import id.my.selo.sekost.core.designsystem.component.SekostTopBar
import id.my.selo.sekost.core.designsystem.component.StatusDot
import id.my.selo.sekost.core.designsystem.component.ToneBadge
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.illustration.DoorIllustration
import id.my.selo.sekost.core.designsystem.illustration.PaidIllustration
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.domain.RoomOverview

@Composable
fun BillingRoute(onOpenRoom: (Long) -> Unit, viewModel: BillingViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BillingScreen(
        state = state,
        onPrevious = viewModel::previousMonth,
        onNext = viewModel::nextMonth,
        onFilter = viewModel::setFilter,
        onMarkPaid = viewModel::markPaid,
        onMarkUnpaid = viewModel::markUnpaid,
        onOpenRoom = onOpenRoom,
    )
}

@Composable
fun BillingScreen(
    state: BillingUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFilter: (BillingFilter) -> Unit,
    onMarkPaid: (RoomOverview) -> Unit,
    onMarkUnpaid: (RoomOverview) -> Unit,
    onOpenRoom: (Long) -> Unit,
) {
    val gutter = SekostTheme.spacing.screenGutter
    LazyColumn(Modifier.fillMaxSize().testTag("billing_list"), contentPadding = PaddingValues(bottom = SekostTheme.spacing.huge)) {
        item { SekostTopBar(title = stringResource(R.string.billing_title), overline = stringResource(R.string.billing_overline)) }
        item { MonthSwitcher(state, onPrevious, onNext, Modifier.padding(horizontal = gutter)) }
        item { SummaryCard(state, Modifier.padding(horizontal = gutter, vertical = SekostTheme.spacing.md)) }
        item {
            SegmentedPicker(
                segments = listOf(
                    Segment(BillingFilter.UNPAID, "${stringResource(R.string.billing_filter_unpaid)} · ${state.summary.unpaidCount}"),
                    Segment(BillingFilter.PAID, "${stringResource(R.string.billing_filter_paid)} · ${state.billable.count { it.isPaid }}"),
                    Segment(BillingFilter.ALL, stringResource(R.string.billing_filter_all)),
                ),
                selected = state.filter,
                onSelect = onFilter,
                modifier = Modifier.padding(horizontal = gutter).padding(bottom = SekostTheme.spacing.md),
            )
        }
        when {
            state.billable.isEmpty() -> item {
                EmptyState(
                    illustration = { DoorIllustration() },
                    title = stringResource(R.string.billing_empty_title),
                    body = stringResource(R.string.billing_empty_body),
                )
            }
            state.visible.isEmpty() && state.filter == BillingFilter.UNPAID -> item {
                EmptyState(
                    illustration = { PaidIllustration() },
                    title = stringResource(R.string.billing_all_paid_title),
                    body = stringResource(R.string.billing_all_paid_body, state.period.displayName()),
                )
            }
            state.visible.isEmpty() -> item {
                Text(
                    stringResource(R.string.billing_none_paid),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SekostTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(SekostTheme.spacing.xxl),
                )
            }
        }
        items(state.visible, key = { it.room.id }) { overview ->
            BillRow(
                overview = overview,
                isLate = state.isLate(overview),
                onClick = { onOpenRoom(overview.room.id) },
                onMarkPaid = { onMarkPaid(overview) },
                onMarkUnpaid = { onMarkUnpaid(overview) },
                modifier = Modifier.padding(horizontal = gutter, vertical = SekostTheme.spacing.xs).animateItem(),
            )
        }
    }
}

@Composable
private fun MonthSwitcher(state: BillingUiState, onPrevious: () -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SekostTheme.colors
    val buttonColors = IconButtonDefaults.iconButtonColors(containerColor = colors.surfaceRaised, contentColor = colors.textPrimary)
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious, colors = buttonColors, modifier = Modifier.testTag("billing_prev")) {
            Icon(SekostIcons.ChevronLeft, stringResource(R.string.billing_prev_month))
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                state.period.displayName(),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                modifier = Modifier.testTag("billing_period"),
            )
            Text(
                stringResource(R.string.billing_due_info, state.dueDay),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }
        IconButton(onClick = onNext, enabled = state.canGoNext, colors = buttonColors, modifier = Modifier.testTag("billing_next")) {
            Icon(SekostIcons.ChevronRight, stringResource(R.string.billing_next_month))
        }
    }
}

@Composable
private fun SummaryCard(state: BillingUiState, modifier: Modifier = Modifier) {
    val colors = SekostTheme.colors
    val summary = state.summary
    SekostCard(modifier.fillMaxWidth().animateContentSize()) {
        Text(stringResource(R.string.billing_collected), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                Rupiah.format(summary.collectedIncome),
                style = SekostTheme.typography.amount,
                color = colors.textPrimary,
                modifier = Modifier.testTag("billing_collected"),
            )
            Text(
                " / ${Rupiah.compact(summary.expectedIncome)}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary,
                modifier = Modifier.padding(bottom = SekostTheme.spacing.xs),
            )
        }
        Spacer(Modifier.height(SekostTheme.spacing.md))
        ProgressTrack(summary.collectionRate, colors.paid.content, colors.surfaceSunken)
        Spacer(Modifier.height(SekostTheme.spacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(SekostTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Legend(colors.paid.content, stringResource(R.string.billing_paid_count, summary.paidCount))
            Legend(colors.unpaid.content, stringResource(R.string.billing_unpaid_count, summary.unpaidCount))
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.billing_outstanding, Rupiah.compact(summary.outstandingIncome)),
                style = MaterialTheme.typography.labelMedium,
                color = colors.unpaid.content,
            )
        }
    }
}

@Composable
private fun Legend(color: androidx.compose.ui.graphics.Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StatusDot(color)
        Spacer(Modifier.width(SekostTheme.spacing.xs + SekostTheme.spacing.xxs))
        Text(text, style = MaterialTheme.typography.labelMedium, color = SekostTheme.colors.textSecondary)
    }
}

@Composable
private fun BillRow(
    overview: RoomOverview,
    isLate: Boolean,
    onClick: () -> Unit,
    onMarkPaid: () -> Unit,
    onMarkUnpaid: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SekostTheme.colors
    SekostCard(modifier.fillMaxWidth().testTag("bill_${overview.room.id}"), onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    overview.tenant?.name ?: overview.room.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOfNotNull(overview.room.label.takeIf { overview.tenant != null }, overview.property?.name).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            when {
                overview.isPaid -> ToneBadge(stringResource(R.string.payment_paid), colors.paid, icon = SekostIcons.Check)
                isLate -> ToneBadge(stringResource(R.string.payment_late), colors.complaint)
                else -> ToneBadge(stringResource(R.string.payment_unpaid), colors.unpaid)
            }
        }
        Spacer(Modifier.height(SekostTheme.spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    Rupiah.format(overview.payment?.amount ?: overview.fee.amount),
                    style = SekostTheme.typography.amountSmall,
                    color = colors.textPrimary,
                )
                overview.payment?.let {
                    Text(
                        stringResource(R.string.room_paid_on, it.paidOn.displayName()),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                    )
                }
            }
            if (overview.isPaid) {
                SekostOutlinedButton(
                    stringResource(R.string.room_mark_unpaid),
                    onClick = onMarkUnpaid,
                    icon = SekostIcons.Undo,
                    contentColor = colors.textSecondary,
                    modifier = Modifier.testTag("bill_unpay_${overview.room.id}"),
                )
            } else {
                SekostOutlinedButton(
                    stringResource(R.string.room_mark_paid),
                    onClick = onMarkPaid,
                    icon = SekostIcons.Check,
                    contentColor = colors.brand,
                    modifier = Modifier.testTag("bill_pay_${overview.room.id}"),
                )
            }
        }
    }
}
