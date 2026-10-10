package id.my.selo.sekost.feature.property

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import id.my.selo.sekost.R
import id.my.selo.sekost.core.designsystem.component.SekostButton
import id.my.selo.sekost.core.designsystem.component.SekostSheet
import id.my.selo.sekost.core.designsystem.component.SekostTextField
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.model.Room

/**
 * Create or rename a room. Fee and status are edited on the room detail screen where their
 * consequences (inherited fee, tenant checkout) can be explained in context.
 */
@Composable
fun RoomEditorSheet(
    initial: Room?,
    propertyId: Long,
    onDismiss: () -> Unit,
    onSave: (Room) -> Unit,
    suggestedLabel: String = "",
) {
    var label by rememberSaveable { mutableStateOf(initial?.label ?: suggestedLabel) }
    var floor by rememberSaveable { mutableStateOf(initial?.floor.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(initial?.notes.orEmpty()) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val labelError = label.isBlank()

    SekostSheet(title = stringResource(if (initial == null) R.string.room_new else R.string.room_edit), onDismiss = onDismiss) {
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
