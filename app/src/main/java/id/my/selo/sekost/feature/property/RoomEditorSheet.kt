package id.my.selo.sekost.feature.property

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import id.my.selo.sekost.R
import id.my.selo.sekost.core.designsystem.component.Segment
import id.my.selo.sekost.core.designsystem.component.SegmentedPicker
import id.my.selo.sekost.core.designsystem.component.SekostButton
import id.my.selo.sekost.core.designsystem.component.SekostSheet
import id.my.selo.sekost.core.designsystem.component.SekostTextField
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.domain.RoomBatch
import id.my.selo.sekost.core.model.Room

private const val DEFAULT_PREFIX = "Kamar"
private const val DEFAULT_BATCH_COUNT = "10"

/**
 * Create or rename a room. Fee and status are edited on the room detail screen where their
 * consequences (inherited fee, tenant checkout) can be explained in context.
 *
 * When creating and [onSaveBatch] is provided, the sheet also offers a "many rooms" mode that
 * generates sequential labels ("Kamar 01" … "Kamar 10"), skipping any in [existingLabels].
 */
@Composable
fun RoomEditorSheet(
    initial: Room?,
    propertyId: Long,
    onDismiss: () -> Unit,
    onSave: (Room) -> Unit,
    suggestedLabel: String = "",
    existingLabels: List<String> = emptyList(),
    onSaveBatch: ((labels: List<String>, floor: String) -> Unit)? = null,
) {
    var batchMode by rememberSaveable { mutableStateOf(false) }
    var label by rememberSaveable { mutableStateOf(initial?.label ?: suggestedLabel) }
    var floor by rememberSaveable { mutableStateOf(initial?.floor.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(initial?.notes.orEmpty()) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val labelError = label.isBlank()

    SekostSheet(title = stringResource(if (initial == null) R.string.room_new else R.string.room_edit), onDismiss = onDismiss) {
        if (initial == null && onSaveBatch != null) {
            SegmentedPicker(
                segments = listOf(
                    Segment(false, stringResource(R.string.room_mode_single), SekostIcons.Door),
                    Segment(true, stringResource(R.string.room_mode_batch), SekostIcons.Kost),
                ),
                selected = batchMode,
                onSelect = { batchMode = it },
                modifier = Modifier.testTag("room_mode"),
            )
        }
        if (batchMode && onSaveBatch != null) {
            RoomBatchFields(
                existingLabels = existingLabels,
                floor = floor,
                onFloorChange = { floor = it },
                onSave = onSaveBatch,
            )
            return@SekostSheet
        }
        SekostTextField(
            value = label,
            onValueChange = { label = it },
            label = stringResource(R.string.room_label),
            placeholder = stringResource(R.string.room_label_hint),
            leadingIcon = SekostIcons.Door,
            isError = showErrors && labelError,
            supportingText = if (showErrors && labelError) stringResource(R.string.error_required) else null,
            modifier = Modifier.testTag("room_label"),
        )
        SekostTextField(
            value = floor,
            onValueChange = { floor = it },
            label = stringResource(R.string.room_floor),
            leadingIcon = SekostIcons.Kost,
        )
        SekostTextField(
            value = notes,
            onValueChange = { notes = it },
            label = stringResource(R.string.room_notes),
            singleLine = false,
            minLines = 2,
        )
        SekostButton(
            text = stringResource(R.string.action_save),
            icon = SekostIcons.Check,
            onClick = {
                showErrors = true
                if (!labelError) {
                    onSave(
                        (initial ?: Room(propertyId = propertyId, label = label)).copy(label = label, floor = floor, notes = notes),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = SekostTheme.spacing.sm).testTag("room_save"),
        )
    }
}

@Composable
private fun RoomBatchFields(
    existingLabels: List<String>,
    floor: String,
    onFloorChange: (String) -> Unit,
    onSave: (labels: List<String>, floor: String) -> Unit,
) {
    var prefix by rememberSaveable { mutableStateOf(DEFAULT_PREFIX) }
    // The start number follows the prefix ("Kamar" → after the highest "Kamar N") until edited by hand.
    var startEdited by rememberSaveable { mutableStateOf(false) }
    var startInput by rememberSaveable { mutableStateOf(RoomBatch.nextNumber(DEFAULT_PREFIX, existingLabels).toString()) }
    var countInput by rememberSaveable { mutableStateOf(DEFAULT_BATCH_COUNT) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val start = startInput.toIntOrNull()
    val count = countInput.toIntOrNull()
    val startError = start == null
    val countError = count == null || count !in 1..RoomBatch.MAX_COUNT
    val labels = remember(prefix, start, count, existingLabels) {
        if (start == null || countError) emptyList() else RoomBatch.labels(prefix, start, count!!, existingLabels)
    }
    val skipped = if (start == null || countError) 0 else count!! - labels.size
    val numberKeyboard = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)

    SekostTextField(
        value = prefix,
        onValueChange = {
            prefix = it
            if (!startEdited) startInput = RoomBatch.nextNumber(it, existingLabels).toString()
        },
        label = stringResource(R.string.room_batch_prefix),
        placeholder = DEFAULT_PREFIX,
        leadingIcon = SekostIcons.Door,
        modifier = Modifier.testTag("room_batch_prefix"),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(SekostTheme.spacing.md)) {
        SekostTextField(
            value = startInput,
            onValueChange = { input ->
                startInput = input.filter(Char::isDigit).take(4)
                startEdited = true
            },
            label = stringResource(R.string.room_batch_start),
            isError = showErrors && startError,
            supportingText = if (showErrors && startError) stringResource(R.string.error_required) else null,
            keyboardOptions = numberKeyboard,
            modifier = Modifier.weight(1f).testTag("room_batch_start"),
        )
        SekostTextField(
            value = countInput,
            onValueChange = { input -> countInput = input.filter(Char::isDigit).take(3) },
            label = stringResource(R.string.room_batch_count),
            isError = showErrors && countError,
            supportingText = if (showErrors && countError) {
                stringResource(R.string.room_batch_count_error, RoomBatch.MAX_COUNT)
            } else {
                null
            },
            keyboardOptions = numberKeyboard,
            modifier = Modifier.weight(1f).testTag("room_batch_count"),
        )
    }
    SekostTextField(
        value = floor,
        onValueChange = onFloorChange,
        label = stringResource(R.string.room_floor),
        leadingIcon = SekostIcons.Kost,
        supportingText = stringResource(R.string.room_batch_floor_hint),
    )
    BatchPreview(labels, skipped, valid = !startError && !countError)
    SekostButton(
        text = if (labels.isEmpty()) {
            stringResource(R.string.property_add_room)
        } else {
            stringResource(R.string.room_batch_save, labels.size)
        },
        icon = SekostIcons.Check,
        // Invalid input stays tappable to reveal errors; a valid range with nothing new does not.
        enabled = startError || countError || labels.isNotEmpty(),
        onClick = {
            showErrors = true
            if (labels.isNotEmpty()) onSave(labels, floor)
        },
        modifier = Modifier.fillMaxWidth().padding(top = SekostTheme.spacing.sm).testTag("room_batch_save"),
    )
}

@Composable
private fun BatchPreview(labels: List<String>, skipped: Int, valid: Boolean) {
    if (!valid) return
    Column(
        verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xs),
        modifier = Modifier.fillMaxWidth().testTag("room_batch_preview"),
    ) {
        if (labels.isEmpty()) {
            Text(
                stringResource(R.string.room_batch_none),
                style = MaterialTheme.typography.bodySmall,
                color = SekostTheme.colors.textSecondary,
            )
            return@Column
        }
        Text(
            if (labels.size == 1) labels.first() else stringResource(R.string.room_batch_range, labels.first(), labels.last()),
            style = MaterialTheme.typography.titleSmall,
            color = SekostTheme.colors.textPrimary,
        )
        if (skipped > 0) {
            Text(
                stringResource(R.string.room_batch_skipped, skipped),
                style = MaterialTheme.typography.bodySmall,
                color = SekostTheme.colors.textSecondary,
            )
        }
    }
}
