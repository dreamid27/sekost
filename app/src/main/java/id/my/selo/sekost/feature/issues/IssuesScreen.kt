package id.my.selo.sekost.feature.issues

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.selo.sekost.R
import id.my.selo.sekost.core.designsystem.component.EmptyState
import id.my.selo.sekost.core.designsystem.component.Segment
import id.my.selo.sekost.core.designsystem.component.SegmentedPicker
import id.my.selo.sekost.core.designsystem.component.SekostTextField
import id.my.selo.sekost.core.designsystem.component.SekostTopBar
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.illustration.CalmIllustration
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.model.IssueStatus
import id.my.selo.sekost.core.model.IssueType
import id.my.selo.sekost.core.ui.IssueCard
import id.my.selo.sekost.core.ui.icon
import id.my.selo.sekost.core.ui.labelRes
import id.my.selo.sekost.feature.room.IssueSheet

@Composable
fun IssuesRoute(onOpenRoom: (Long) -> Unit, viewModel: IssuesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    IssuesScreen(
        state = state,
        onStatus = viewModel::setStatus,
        onType = viewModel::setType,
        onResolve = viewModel::resolve,
        onReopen = viewModel::reopen,
        onReport = viewModel::report,
        onOpenRoom = onOpenRoom,
    )
}

@Composable
fun IssuesScreen(
    state: IssuesUiState,
    onStatus: (IssueStatus) -> Unit,
    onType: (IssueType?) -> Unit,
    onResolve: (Long) -> Unit,
    onReopen: (Long) -> Unit,
    onReport: (Long, IssueType, String, String) -> Unit,
    onOpenRoom: (Long) -> Unit,
) {
    var showSheet by rememberSaveable { mutableStateOf(false) }
    val gutter = SekostTheme.spacing.screenGutter

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().testTag("issues_list"), contentPadding = PaddingValues(bottom = SekostTheme.spacing.huge * 2)) {
            item { SekostTopBar(title = stringResource(R.string.issues_title), overline = stringResource(R.string.issues_overline)) }
            item {
                SegmentedPicker(
                    segments = listOf(
                        Segment(IssueStatus.OPEN, "${stringResource(R.string.issue_status_open)} · ${state.openCount}"),
                        Segment(IssueStatus.RESOLVED, "${stringResource(R.string.issue_status_resolved)} · ${state.resolvedCount}"),
                    ),
                    selected = state.status,
                    onSelect = onStatus,
                    modifier = Modifier.padding(horizontal = gutter),
                )
            }
            item {
                Row(
                    Modifier.padding(horizontal = gutter, vertical = SekostTheme.spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(SekostTheme.spacing.sm),
                ) {
                    TypeChip(stringResource(R.string.issue_type_all), state.type == null, null) { onType(null) }
                    IssueType.entries.forEach { t ->
                        TypeChip(stringResource(t.labelRes), state.type == t, t) { onType(if (state.type == t) null else t) }
                    }
                }
            }
            if (state.items.isEmpty()) {
                item {
                    if (state.status == IssueStatus.OPEN) {
                        EmptyState(
                            illustration = { CalmIllustration() },
                            title = stringResource(R.string.issues_empty_title),
                            body = stringResource(R.string.issues_empty_body),
                        )
                    } else {
                        Text(
                            stringResource(R.string.issues_empty_resolved),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SekostTheme.colors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(SekostTheme.spacing.xxl),
                        )
                    }
                }
            }
            items(state.items, key = { it.issue.id }) { item ->
                IssueCard(
                    issue = item.issue,
                    context = item.room?.let { "${it.label} · ${it.propertyName}" },
                    onClick = { onOpenRoom(item.issue.roomId) },
                    onResolve = { onResolve(item.issue.id) },
                    onReopen = { onReopen(item.issue.id) },
                    modifier = Modifier.padding(horizontal = gutter, vertical = SekostTheme.spacing.xs).animateItem(),
                )
            }
        }

        if (state.rooms.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = { showSheet = true },
                icon = { Icon(SekostIcons.Plus, null) },
                text = { Text(stringResource(R.string.room_add_issue), style = MaterialTheme.typography.labelLarge) },
                containerColor = SekostTheme.colors.accent,
                contentColor = SekostTheme.colors.onAccent,
                shape = RoundedCornerShape(SekostTheme.radii.pill),
                modifier = Modifier.align(Alignment.BottomEnd).padding(SekostTheme.spacing.xl).testTag("issues_add"),
            )
        }
    }

    if (showSheet) {
        var roomId by rememberSaveable { mutableStateOf(state.rooms.firstOrNull()?.roomId) }
        IssueSheet(
            onDismiss = { showSheet = false },
            canSave = roomId != null,
            onSave = { type, title, description ->
                roomId?.let { onReport(it, type, title, description) }
                showSheet = false
            },
            roomPicker = { RoomPicker(state.rooms, roomId) { roomId = it } },
        )
    }
}

@Composable
private fun TypeChip(label: String, selected: Boolean, type: IssueType?, onClick: () -> Unit) {
    val colors = SekostTheme.colors
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = type?.let { { Icon(it.icon, null, modifier = Modifier.size(SekostTheme.sizes.iconSm)) } },
        shape = RoundedCornerShape(SekostTheme.radii.pill),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.surfaceRaised,
            labelColor = colors.textSecondary,
            iconColor = colors.textSecondary,
            selectedContainerColor = colors.textPrimary,
            selectedLabelColor = colors.surface,
            selectedLeadingIconColor = colors.surface,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = colors.outline,
            selectedBorderColor = colors.textPrimary,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomPicker(rooms: List<RoomOption>, selected: Long?, onSelect: (Long) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val current = rooms.firstOrNull { it.roomId == selected }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        SekostTextField(
            value = current?.let { "${it.label} · ${it.propertyName}" }.orEmpty(),
            onValueChange = {},
            label = stringResource(R.string.issue_room),
            placeholder = stringResource(R.string.issue_room_pick),
            leadingIcon = SekostIcons.Door,
            readOnly = true,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).testTag("issue_room_picker"),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = SekostTheme.colors.surfaceRaised,
        ) {
            rooms.forEach { option ->
                DropdownMenuItem(
                    text = { Text("${option.label} · ${option.propertyName}", style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        onSelect(option.roomId)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
