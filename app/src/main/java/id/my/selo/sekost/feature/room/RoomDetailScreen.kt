package id.my.selo.sekost.feature.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.selo.sekost.R
import id.my.selo.sekost.core.common.Rupiah
import id.my.selo.sekost.core.common.displayName
import id.my.selo.sekost.core.designsystem.component.ButtonTone
import id.my.selo.sekost.core.designsystem.component.ConfirmDialog
import id.my.selo.sekost.core.designsystem.component.IconTile
import id.my.selo.sekost.core.designsystem.component.SectionHeader
import id.my.selo.sekost.core.designsystem.component.Segment
import id.my.selo.sekost.core.designsystem.component.SegmentedPicker
import id.my.selo.sekost.core.designsystem.component.SekostButton
import id.my.selo.sekost.core.designsystem.component.SekostCard
import id.my.selo.sekost.core.designsystem.component.SekostOutlinedButton
import id.my.selo.sekost.core.designsystem.component.SekostTextButton
import id.my.selo.sekost.core.designsystem.component.SekostTopBar
import id.my.selo.sekost.core.designsystem.component.ToneBadge
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.model.IssueType
import id.my.selo.sekost.core.model.Room
import id.my.selo.sekost.core.model.RoomStatus
import id.my.selo.sekost.core.model.Tenant
import id.my.selo.sekost.core.ui.IssueCard
import id.my.selo.sekost.core.ui.initialsOf
import id.my.selo.sekost.core.ui.labelRes
import id.my.selo.sekost.core.ui.tone
import id.my.selo.sekost.feature.property.HeaderAction
import id.my.selo.sekost.feature.property.RoomEditorSheet
import java.time.LocalDate

@Composable
fun RoomDetailRoute(onBack: () -> Unit, viewModel: RoomDetailViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (val s = state) {
        RoomDetailUiState.Loading -> Unit
        RoomDetailUiState.NotFound -> LaunchedEffect(Unit) { onBack() }
        is RoomDetailUiState.Ready -> RoomDetailScreen(
            state = s,
            actions = RoomDetailActions(
                onBack = onBack,
                onSetStatus = viewModel::setStatus,
                onCheckIn = viewModel::checkIn,
                onUpdateTenant = viewModel::updateTenant,
                onCheckOut = viewModel::checkOut,
                onSetCustomFee = viewModel::setCustomFee,
                onMarkPaid = viewModel::markPaid,
                onMarkUnpaid = viewModel::markUnpaid,
                onReportIssue = viewModel::reportIssue,
                onResolveIssue = viewModel::resolveIssue,
                onReopenIssue = viewModel::reopenIssue,
                onDeleteIssue = viewModel::deleteIssue,
                onSaveRoom = viewModel::saveRoom,
                onDeleteRoom = { viewModel.deleteRoom(onBack) },
            ),
        )
    }
}

/** Bundles the screen's callbacks so the composable signature stays readable. */
data class RoomDetailActions(
    val onBack: () -> Unit,
    val onSetStatus: (RoomStatus) -> Unit,
    val onCheckIn: (String, String, LocalDate) -> Unit,
    val onUpdateTenant: (Tenant) -> Unit,
    val onCheckOut: (RoomStatus) -> Unit,
    val onSetCustomFee: (Long?) -> Unit,
    val onMarkPaid: () -> Unit,
    val onMarkUnpaid: () -> Unit,
    val onReportIssue: (IssueType, String, String) -> Unit,
    val onResolveIssue: (Long) -> Unit,
    val onReopenIssue: (Long) -> Unit,
    val onDeleteIssue: (Long) -> Unit,
    val onSaveRoom: (Room) -> Unit,
    val onDeleteRoom: () -> Unit,
)

private enum class RoomSheet { None, EditRoom, CheckIn, EditTenant, Fee, Issue }

@Composable
fun RoomDetailScreen(state: RoomDetailUiState.Ready, actions: RoomDetailActions) {
    var sheet by rememberSaveable { mutableStateOf(RoomSheet.None) }
    var pendingCheckoutStatus by rememberSaveable { mutableStateOf<RoomStatus?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var pendingIssueDelete by rememberSaveable { mutableStateOf<Long?>(null) }
    val gutter = SekostTheme.spacing.screenGutter
    val room = state.room

    fun requestStatus(target: RoomStatus) {
        when {
            target == room.status -> Unit
            target == RoomStatus.OCCUPIED && state.tenant == null -> sheet = RoomSheet.CheckIn
            target != RoomStatus.OCCUPIED && state.tenant != null -> pendingCheckoutStatus = target
            else -> actions.onSetStatus(target)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("room_detail"),
        contentPadding = PaddingValues(bottom = SekostTheme.spacing.huge),
    ) {
        item {
            SekostTopBar(
                title = room.label,
                overline = listOfNotNull(state.property?.name, room.floor.takeIf { it.isNotBlank() }).joinToString(" · "),
                onBack = actions.onBack,
                actions = {
                    HeaderAction(
                        SekostIcons.Edit,
                        stringResource(R.string.action_edit),
                        Modifier.testTag("room_edit"),
                    ) { sheet = RoomSheet.EditRoom }
                    HeaderAction(
                        SekostIcons.Trash,
                        stringResource(R.string.action_delete),
                        Modifier.testTag("room_delete"),
                    ) { confirmDelete = true }
                },
            )
        }

        item {
            Column(Modifier.padding(horizontal = gutter), verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.md)) {
                StatusCard(room.status, onSelect = ::requestStatus)
                FeeCard(state, onEdit = { sheet = RoomSheet.Fee })
                OccupantCard(
                    state = state,
                    onAdd = { sheet = RoomSheet.CheckIn },
                    onEdit = { sheet = RoomSheet.EditTenant },
                    onCheckOut = { pendingCheckoutStatus = RoomStatus.VACANT },
                )
                if (state.tenant != null) {
                    PaymentCard(state, onMarkPaid = actions.onMarkPaid, onMarkUnpaid = actions.onMarkUnpaid)
                }
                if (room.notes.isNotBlank()) {
                    SekostCard(Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.room_notes),
                            style = MaterialTheme.typography.labelMedium,
                            color = SekostTheme.colors.textSecondary,
                        )
                        Text(
                            room.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SekostTheme.colors.textPrimary,
                            modifier = Modifier.padding(top = SekostTheme.spacing.xs),
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.room_issues),
                actionLabel = stringResource(R.string.room_add_issue),
                actionIcon = SekostIcons.Plus,
                onAction = { sheet = RoomSheet.Issue },
                modifier = Modifier.padding(horizontal = gutter).testTag("room_add_issue"),
            )
        }
        if (state.issues.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.room_no_issues),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SekostTheme.colors.textTertiary,
                    modifier = Modifier.padding(horizontal = gutter),
                )
            }
        }
        items(state.issues, key = { "issue-${it.id}" }) { issue ->
            IssueCard(
                issue = issue,
                onResolve = { actions.onResolveIssue(issue.id) },
                onReopen = { actions.onReopenIssue(issue.id) },
                onDelete = { pendingIssueDelete = issue.id },
                modifier = Modifier.padding(horizontal = gutter, vertical = SekostTheme.spacing.xs),
            )
        }

        if (state.paymentHistory.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.room_payment_history), Modifier.padding(horizontal = gutter)) }
            item {
                SekostCard(Modifier.padding(horizontal = gutter).fillMaxWidth()) {
                    state.paymentHistory.forEachIndexed { i, payment ->
                        if (i > 0) {
                            HorizontalDivider(
                                color = SekostTheme.colors.outline,
                                modifier = Modifier.padding(vertical = SekostTheme.spacing.sm),
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    payment.period.displayName(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SekostTheme.colors.textPrimary,
                                )
                                Text(
                                    stringResource(R.string.room_paid_on, payment.paidOn.displayName()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SekostTheme.colors.textTertiary,
                                )
                            }
                            Text(
                                Rupiah.format(payment.amount),
                                style = MaterialTheme.typography.titleSmall,
                                color = SekostTheme.colors.paid.content,
                            )
                        }
                    }
                }
            }
        }

        if (state.pastTenants.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.room_past_tenants), Modifier.padding(horizontal = gutter)) }
            items(state.pastTenants, key = { "tenant-${it.id}" }) { tenant ->
                Row(
                    Modifier.padding(horizontal = gutter, vertical = SekostTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(tenant.name, small = true)
                    Spacer(Modifier.width(SekostTheme.spacing.md))
                    Column {
                        Text(tenant.name, style = MaterialTheme.typography.titleSmall, color = SekostTheme.colors.textPrimary)
                        Text(
                            "${tenant.checkInDate.displayName()} – ${tenant.checkOutDate?.displayName().orEmpty()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SekostTheme.colors.textTertiary,
                        )
                    }
                }
            }
        }
    }

    when (sheet) {
        RoomSheet.None -> Unit
        RoomSheet.EditRoom -> RoomEditorSheet(
            initial = room,
            propertyId = room.propertyId,
            onDismiss = { sheet = RoomSheet.None },
            onSave = {
                actions.onSaveRoom(it)
                sheet = RoomSheet.None
            },
        )
        RoomSheet.CheckIn -> TenantSheet(
            initial = null,
            today = state.today,
            onDismiss = { sheet = RoomSheet.None },
            onSave = { name, phone, date ->
                actions.onCheckIn(name, phone, date)
                sheet = RoomSheet.None
            },
        )
        RoomSheet.EditTenant -> state.tenant?.let { tenant ->
            TenantSheet(
                initial = tenant,
                today = state.today,
                onDismiss = { sheet = RoomSheet.None },
                onSave = { name, phone, date ->
                    actions.onUpdateTenant(tenant.copy(name = name, phone = phone, checkInDate = date))
                    sheet = RoomSheet.None
                },
            )
        }
        RoomSheet.Fee -> FeeSheet(
            customFee = room.customMonthlyFee,
            inherited = state.inheritedFee,
            onDismiss = { sheet = RoomSheet.None },
            onSave = {
                actions.onSetCustomFee(it)
                sheet = RoomSheet.None
            },
        )
        RoomSheet.Issue -> IssueSheet(
            onDismiss = { sheet = RoomSheet.None },
            onSave = { type, title, description ->
                actions.onReportIssue(type, title, description)
                sheet = RoomSheet.None
            },
        )
    }

    pendingCheckoutStatus?.let { target ->
        ConfirmDialog(
            title = stringResource(R.string.room_checkout_title),
            message = stringResource(R.string.room_checkout_message, state.tenant?.name.orEmpty(), stringResource(target.labelRes)),
            confirmLabel = stringResource(R.string.room_check_out),
            onConfirm = {
                actions.onCheckOut(target)
                pendingCheckoutStatus = null
            },
            onDismiss = { pendingCheckoutStatus = null },
        )
    }
    pendingIssueDelete?.let { id ->
        ConfirmDialog(
            title = stringResource(R.string.issue_delete_title),
            message = stringResource(R.string.issue_delete_message, state.issues.firstOrNull { it.id == id }?.title.orEmpty()),
            confirmLabel = stringResource(R.string.action_delete),
            destructive = true,
            onConfirm = {
                actions.onDeleteIssue(id)
                pendingIssueDelete = null
            },
            onDismiss = { pendingIssueDelete = null },
        )
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.room_delete_title),
            message = stringResource(R.string.room_delete_message, room.label),
            confirmLabel = stringResource(R.string.action_delete),
            destructive = true,
            onConfirm = {
                confirmDelete = false
                actions.onDeleteRoom()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun StatusCard(status: RoomStatus, onSelect: (RoomStatus) -> Unit) {
    SekostCard(Modifier.fillMaxWidth().testTag("room_status_card")) {
        Text(stringResource(R.string.room_status), style = MaterialTheme.typography.labelLarge, color = SekostTheme.colors.textSecondary)
        Spacer(Modifier.height(SekostTheme.spacing.md))
        SegmentedPicker(
            segments = RoomStatus.entries.map { Segment(it, stringResource(it.labelRes), tone = it.tone) },
            selected = status,
            onSelect = onSelect,
        )
        if (status == RoomStatus.MAINTENANCE) {
            Text(
                stringResource(R.string.room_maintenance_hint),
                style = MaterialTheme.typography.bodySmall,
                color = SekostTheme.colors.maintenance.content,
                modifier = Modifier.padding(top = SekostTheme.spacing.sm),
            )
        }
    }
}

@Composable
private fun FeeCard(state: RoomDetailUiState.Ready, onEdit: () -> Unit) {
    val colors = SekostTheme.colors
    SekostCard(Modifier.fillMaxWidth().testTag("room_fee_card")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.room_fee),
                style = MaterialTheme.typography.labelLarge,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            SekostTextButton(
                stringResource(R.string.room_change_fee),
                onClick = onEdit,
                icon = SekostIcons.Edit,
                modifier = Modifier.testTag("room_fee_edit"),
            )
        }
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    Rupiah.format(state.fee.amount),
                    style = SekostTheme.typography.amount,
                    color = colors.textPrimary,
                    modifier = Modifier.testTag("room_fee_amount"),
                )
                Text(
                    " ${stringResource(R.string.per_month)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(bottom = SekostTheme.spacing.xs),
                )
            }
            Text(
                stringResource(state.fee.source.labelRes),
                style = MaterialTheme.typography.bodySmall,
                color = colors.brand,
                modifier = Modifier.testTag("room_fee_source"),
            )
        }
    }
}

@Composable
private fun OccupantCard(state: RoomDetailUiState.Ready, onAdd: () -> Unit, onEdit: () -> Unit, onCheckOut: () -> Unit) {
    val colors = SekostTheme.colors
    val tenant = state.tenant
    SekostCard(Modifier.fillMaxWidth().testTag("room_occupant_card")) {
        Text(stringResource(R.string.room_occupant), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
        Spacer(Modifier.height(SekostTheme.spacing.md))
        if (tenant == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(SekostIcons.Key, colors.vacant)
                Spacer(Modifier.width(SekostTheme.spacing.md))
                Text(
                    stringResource(R.string.room_no_occupant),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(SekostTheme.spacing.md))
            SekostButton(
                stringResource(R.string.room_add_occupant),
                onClick = onAdd,
                icon = SekostIcons.Plus,
                modifier = Modifier.fillMaxWidth().testTag("room_add_occupant"),
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(tenant.name)
                Spacer(Modifier.width(SekostTheme.spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(
                        tenant.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("room_tenant_name"),
                    )
                    if (tenant.phone.isNotBlank()) {
                        Text(tenant.phone, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                    Text(
                        stringResource(R.string.room_since, tenant.checkInDate.displayName()),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                    )
                }
            }
            Spacer(Modifier.height(SekostTheme.spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(SekostTheme.spacing.sm)) {
                SekostOutlinedButton(
                    stringResource(R.string.action_edit),
                    onClick = onEdit,
                    icon = SekostIcons.Edit,
                    modifier = Modifier.weight(1f).testTag("room_edit_tenant"),
                )
                SekostOutlinedButton(
                    stringResource(R.string.room_check_out),
                    onClick = onCheckOut,
                    icon = SekostIcons.CheckOut,
                    contentColor = colors.unpaid.content,
                    modifier = Modifier.weight(1f).testTag("room_checkout"),
                )
            }
        }
    }
}

@Composable
private fun PaymentCard(state: RoomDetailUiState.Ready, onMarkPaid: () -> Unit, onMarkUnpaid: () -> Unit) {
    val colors = SekostTheme.colors
    val payment = state.currentPayment
    SekostCard(Modifier.fillMaxWidth().testTag("room_payment_card")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.room_payment, state.period.displayName()),
                style = MaterialTheme.typography.labelLarge,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            when {
                payment != null -> ToneBadge(stringResource(R.string.payment_paid), colors.paid, icon = SekostIcons.Check)
                state.isLate -> ToneBadge(stringResource(R.string.payment_late), colors.complaint)
                else -> ToneBadge(stringResource(R.string.payment_unpaid), colors.unpaid)
            }
        }
        Spacer(Modifier.height(SekostTheme.spacing.sm))
        Text(Rupiah.format(payment?.amount ?: state.fee.amount), style = SekostTheme.typography.amount, color = colors.textPrimary)
        Text(
            if (payment != null) {
                stringResource(R.string.room_paid_on, payment.paidOn.displayName())
            } else {
                stringResource(R.string.room_due_on, state.dueDate.displayName())
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(SekostTheme.spacing.md))
        if (payment == null) {
            SekostButton(
                stringResource(R.string.room_mark_paid),
                onClick = onMarkPaid,
                icon = SekostIcons.Check,
                tone = ButtonTone.Accent,
                modifier = Modifier.fillMaxWidth().testTag("room_mark_paid"),
            )
        } else {
            SekostTextButton(
                stringResource(R.string.room_mark_unpaid),
                onClick = onMarkUnpaid,
                icon = SekostIcons.Undo,
                contentColor = colors.textSecondary,
                modifier = Modifier.testTag("room_mark_unpaid"),
            )
        }
    }
}

@Composable
private fun Avatar(name: String, small: Boolean = false) {
    val size = if (small) SekostTheme.sizes.iconTile - SekostTheme.spacing.sm else SekostTheme.sizes.iconTile + SekostTheme.spacing.xs
    Box(
        Modifier.size(size).clip(CircleShape).background(SekostTheme.colors.accentContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initialsOf(name),
            style = if (small) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
            color = SekostTheme.colors.onAccentContainer,
        )
    }
}
