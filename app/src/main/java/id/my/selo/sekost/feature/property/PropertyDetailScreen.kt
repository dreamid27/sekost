package id.my.selo.sekost.feature.property

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.selo.sekost.R
import id.my.selo.sekost.core.common.Rupiah
import id.my.selo.sekost.core.designsystem.component.ConfirmDialog
import id.my.selo.sekost.core.designsystem.component.EmptyState
import id.my.selo.sekost.core.designsystem.component.IconTile
import id.my.selo.sekost.core.designsystem.component.SekostCard
import id.my.selo.sekost.core.designsystem.component.SekostTopBar
import id.my.selo.sekost.core.designsystem.component.StatusDot
import id.my.selo.sekost.core.designsystem.component.ToneBadge
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.illustration.DoorIllustration
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.domain.RoomBatch
import id.my.selo.sekost.core.domain.RoomOverview
import id.my.selo.sekost.core.model.RoomStatus
import id.my.selo.sekost.core.ui.labelRes
import id.my.selo.sekost.core.ui.tone

@Composable
fun PropertyDetailRoute(
    onBack: () -> Unit,
    onOpenRoom: (Long) -> Unit,
    viewModel: PropertyDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (val s = state) {
        PropertyDetailUiState.Loading -> Unit
        PropertyDetailUiState.NotFound -> LaunchedEffect(Unit) { onBack() }
        is PropertyDetailUiState.Ready -> PropertyDetailScreen(
            state = s,
            onBack = onBack,
            onOpenRoom = onOpenRoom,
            onFilter = viewModel::setFilter,
            onSaveProperty = viewModel::saveProperty,
            onSaveRoom = viewModel::saveRoom,
            onAddRooms = viewModel::addRooms,
            onDelete = { viewModel.deleteProperty(onBack) },
        )
    }
}

@Composable
fun PropertyDetailScreen(
    state: PropertyDetailUiState.Ready,
    onBack: () -> Unit,
    onOpenRoom: (Long) -> Unit,
    onFilter: (RoomStatus?) -> Unit,
    onSaveProperty: (id.my.selo.sekost.core.model.Property) -> Unit,
    onSaveRoom: (id.my.selo.sekost.core.model.Room) -> Unit,
    onAddRooms: (labels: List<String>, floor: String) -> Unit,
    onDelete: () -> Unit,
) {
    var showPropertyEditor by rememberSaveable { mutableStateOf(false) }
    var showRoomEditor by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val gutter = SekostTheme.spacing.screenGutter
    val property = state.property

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().testTag("room_grid"),
            contentPadding = PaddingValues(start = gutter, end = gutter, bottom = SekostTheme.spacing.huge * 2),
            horizontalArrangement = Arrangement.spacedBy(SekostTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.md),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SekostTopBar(
                    title = property.name,
                    overline = stringResource(property.type.labelRes),
                    onBack = onBack,
                    horizontalPadding = SekostTheme.spacing.none,
                    actions = {
                        HeaderAction(
                            SekostIcons.Edit,
                            stringResource(R.string.action_edit),
                            Modifier.testTag("property_edit"),
                        ) { showPropertyEditor = true }
                        HeaderAction(
                            SekostIcons.Trash,
                            stringResource(R.string.action_delete),
                            Modifier.testTag("property_delete"),
                        ) { confirmDelete = true }
                    },
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) { PropertyInfoCard(state) }
            item(span = { GridItemSpan(maxLineSpan) }) {
                FilterRow(state, onFilter)
            }
            if (state.summary.totalRooms == 0) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        illustration = { DoorIllustration() },
                        title = stringResource(R.string.property_empty_title),
                        body = stringResource(R.string.property_empty_body),
                    )
                }
            } else if (state.rooms.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        stringResource(R.string.property_filter_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SekostTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(SekostTheme.spacing.xxl),
                    )
                }
            }
            items(state.rooms, key = { it.room.id }) { overview ->
                RoomTile(overview, onClick = { onOpenRoom(overview.room.id) })
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showRoomEditor = true },
            icon = { Icon(SekostIcons.Plus, null) },
            text = { Text(stringResource(R.string.property_add_room), style = MaterialTheme.typography.labelLarge) },
            containerColor = SekostTheme.colors.brand,
            contentColor = SekostTheme.colors.onBrand,
            shape = RoundedCornerShape(SekostTheme.radii.pill),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(SekostTheme.spacing.xl)
                .testTag("add_room"),
        )
    }

    if (showPropertyEditor) {
        PropertyEditorSheet(
            initial = property,
            globalFee = state.globalFee,
            onDismiss = { showPropertyEditor = false },
            onSave = {
                onSaveProperty(it)
                showPropertyEditor = false
            },
        )
    }
    if (showRoomEditor) {
        RoomEditorSheet(
            initial = null,
            propertyId = property.id,
            suggestedLabel = RoomBatch.labels("Kamar", RoomBatch.nextNumber("Kamar", state.roomLabels), 1).firstOrNull().orEmpty(),
            existingLabels = state.roomLabels,
            onDismiss = { showRoomEditor = false },
            onSave = {
                onSaveRoom(it)
                showRoomEditor = false
            },
            onSaveBatch = { labels, floor ->
                onAddRooms(labels, floor)
                showRoomEditor = false
            },
        )
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.property_delete_title),
            message = stringResource(R.string.property_delete_message, property.name),
            confirmLabel = stringResource(R.string.action_delete),
            destructive = true,
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
internal fun HeaderAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = SekostTheme.colors.surfaceRaised,
            contentColor = SekostTheme.colors.textPrimary,
        ),
    ) { Icon(icon, contentDescription = label, modifier = Modifier.size(SekostTheme.sizes.iconMd)) }
}

@Composable
private fun PropertyInfoCard(state: PropertyDetailUiState.Ready) {
    val colors = SekostTheme.colors
    val property = state.property
    SekostCard(Modifier.fillMaxWidth()) {
        if (property.address.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(SekostIcons.Home, null, tint = colors.textSecondary, modifier = Modifier.size(SekostTheme.sizes.iconSm))
                Spacer(Modifier.width(SekostTheme.spacing.sm))
                Text(property.address, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
            Spacer(Modifier.height(SekostTheme.spacing.md))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(SekostIcons.Wallet, colors.paid, size = SekostTheme.sizes.iconTile - SekostTheme.spacing.xs)
            Spacer(Modifier.width(SekostTheme.spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.property_default_fee_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                if (property.defaultMonthlyFee != null) {
                    Text(
                        "${Rupiah.format(property.defaultMonthlyFee)} ${stringResource(R.string.per_month)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                    )
                } else {
                    Text(
                        stringResource(R.string.property_fee_follow_global, Rupiah.format(state.globalFee)),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${state.summary.occupied}/${state.summary.totalRooms}",
                    style = SekostTheme.typography.amountSmall,
                    color = colors.textPrimary,
                )
                Text(
                    stringResource(R.string.home_occupied_suffix),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun FilterRow(state: PropertyDetailUiState.Ready, onFilter: (RoomStatus?) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = SekostTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(SekostTheme.spacing.sm),
    ) {
        StatusFilterChip(
            label = "${stringResource(R.string.property_filter_all)} · ${state.summary.totalRooms}",
            selected = state.filter == null,
            dotColor = null,
            onClick = { onFilter(null) },
        )
        RoomStatus.entries.forEach { status ->
            StatusFilterChip(
                label = "${stringResource(status.labelRes)} · ${state.counts[status] ?: 0}",
                selected = state.filter == status,
                dotColor = status.tone.content,
                onClick = { onFilter(if (state.filter == status) null else status) },
                modifier = Modifier.testTag("filter_${status.name.lowercase()}"),
            )
        }
    }
}

@Composable
private fun StatusFilterChip(
    label: String,
    selected: Boolean,
    dotColor: androidx.compose.ui.graphics.Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SekostTheme.colors
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = dotColor?.let { { StatusDot(it) } },
        shape = RoundedCornerShape(SekostTheme.radii.pill),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.surfaceRaised,
            labelColor = colors.textSecondary,
            selectedContainerColor = colors.textPrimary,
            selectedLabelColor = colors.surface,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = colors.outline,
            selectedBorderColor = colors.textPrimary,
        ),
    )
}

@Composable
private fun RoomTile(overview: RoomOverview, onClick: () -> Unit) {
    val colors = SekostTheme.colors
    val room = overview.room
    SekostCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("room_tile_${room.id}"),
        contentPadding = PaddingValues(SekostTheme.spacing.md + SekostTheme.spacing.xxs),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    room.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Always reserve the line so tiles in a row keep equal heights.
                Text(room.floor.ifBlank { " " }, style = MaterialTheme.typography.bodySmall, color = colors.textTertiary, maxLines = 1)
            }
            if (overview.openIssues > 0) {
                ToneBadge(overview.openIssues.toString(), colors.complaint, icon = SekostIcons.Toolbox)
            }
        }
        Spacer(Modifier.height(SekostTheme.spacing.md))
        ToneBadge(stringResource(room.status.labelRes), room.status.tone)
        Spacer(Modifier.height(SekostTheme.spacing.md))
        Text(
            overview.tenant?.name ?: stringResource(R.string.property_no_tenant),
            style = MaterialTheme.typography.bodyMedium,
            color = if (overview.tenant != null) colors.textPrimary else colors.textTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = SekostTheme.spacing.xxs)) {
            Text(
                Rupiah.compact(overview.fee.amount),
                style = MaterialTheme.typography.labelLarge,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            if (overview.isOccupied) {
                StatusDot(if (overview.isPaid) colors.paid.content else colors.unpaid.content)
                Spacer(Modifier.width(SekostTheme.spacing.xs))
                Text(
                    stringResource(if (overview.isPaid) R.string.payment_paid else R.string.payment_unpaid),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (overview.isPaid) colors.paid.content else colors.unpaid.content,
                )
            }
        }
    }
}
