package id.my.selo.sekost.feature.room

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import id.my.selo.sekost.R
import id.my.selo.sekost.core.common.Rupiah
import id.my.selo.sekost.core.common.displayName
import id.my.selo.sekost.core.designsystem.component.ButtonTone
import id.my.selo.sekost.core.designsystem.component.KeyValueRow
import id.my.selo.sekost.core.designsystem.component.MoneyTextField
import id.my.selo.sekost.core.designsystem.component.Segment
import id.my.selo.sekost.core.designsystem.component.SegmentedPicker
import id.my.selo.sekost.core.designsystem.component.SekostButton
import id.my.selo.sekost.core.designsystem.component.SekostOutlinedButton
import id.my.selo.sekost.core.designsystem.component.SekostSheet
import id.my.selo.sekost.core.designsystem.component.SekostTextButton
import id.my.selo.sekost.core.designsystem.component.SekostTextField
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.domain.ResolvedFee
import id.my.selo.sekost.core.model.IssueType
import id.my.selo.sekost.core.model.Tenant
import id.my.selo.sekost.core.ui.icon
import id.my.selo.sekost.core.ui.labelRes
import id.my.selo.sekost.core.ui.tone
import id.my.selo.sekost.feature.property.ToggleRow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Check a new tenant in (when [initial] is null) or edit the current tenant's details. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantSheet(
    initial: Tenant?,
    today: LocalDate,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, checkIn: LocalDate) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(initial?.phone.orEmpty()) }
    var checkInEpochDay by rememberSaveable { mutableLongStateOf((initial?.checkInDate ?: today).toEpochDay()) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val checkIn = LocalDate.ofEpochDay(checkInEpochDay)
    val focusManager = LocalFocusManager.current
    val nameError = name.isBlank()

    SekostSheet(
        title = stringResource(if (initial == null) R.string.tenant_new else R.string.tenant_edit),
        subtitle = if (initial == null) stringResource(R.string.tenant_check_in_subtitle) else null,
        onDismiss = onDismiss,
    ) {
        SekostTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.tenant_name),
            leadingIcon = SekostIcons.Person,
            isError = showErrors && nameError,
            supportingText = if (showErrors && nameError) stringResource(R.string.error_required) else null,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.testTag("tenant_name"),
        )
        SekostTextField(
            value = phone,
            onValueChange = { input -> phone = input.filter { it.isDigit() || it == '+' || it == ' ' || it == '-' } },
            label = stringResource(R.string.tenant_phone),
            leadingIcon = SekostIcons.Phone,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            modifier = Modifier.testTag("tenant_phone"),
        )
        Text(
            stringResource(R.string.tenant_check_in),
            style = MaterialTheme.typography.labelLarge,
            color = SekostTheme.colors.textSecondary,
        )
        SekostOutlinedButton(
            text = if (checkIn == today) stringResource(R.string.tenant_check_in_today, today.displayName()) else checkIn.displayName(),
            onClick = {
                focusManager.clearFocus()
                showDatePicker = true
            },
            icon = SekostIcons.Calendar,
            modifier = Modifier.fillMaxWidth().testTag("tenant_date"),
        )
        SekostButton(
            text = stringResource(R.string.action_save),
            icon = if (initial == null) SekostIcons.Key else SekostIcons.Check,
            onClick = {
                showErrors = true
                if (!nameError) onSave(name, phone, checkIn)
            },
            modifier = Modifier.fillMaxWidth().padding(top = SekostTheme.spacing.sm).testTag("tenant_save"),
        )
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = checkIn.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                SekostTextButton(stringResource(R.string.action_save), onClick = {
                    pickerState.selectedDateMillis?.let {
                        checkInEpochDay = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                    }
                    showDatePicker = false
                })
            },
            dismissButton = {
                SekostTextButton(
                    stringResource(R.string.action_cancel),
                    onClick = { showDatePicker = false },
                    contentColor = SekostTheme.colors.textSecondary,
                )
            },
            colors = DatePickerDefaults.colors(containerColor = SekostTheme.colors.surface),
        ) {
            DatePicker(
                state = pickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = SekostTheme.colors.surface,
                    selectedDayContainerColor = SekostTheme.colors.brand,
                    todayDateBorderColor = SekostTheme.colors.accent,
                ),
            )
        }
    }
}

/** Toggle between the inherited fee and a room-specific override. */
@Composable
fun FeeSheet(
    customFee: Long?,
    inherited: ResolvedFee,
    onDismiss: () -> Unit,
    onSave: (Long?) -> Unit,
) {
    var useCustom by rememberSaveable { mutableStateOf(customFee != null) }
    var digits by rememberSaveable { mutableStateOf(customFee?.toString().orEmpty()) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val error = useCustom && Rupiah.parse(digits) == null

    SekostSheet(
        title = stringResource(R.string.room_fee),
        subtitle = stringResource(R.string.room_fee_sheet_subtitle),
        onDismiss = onDismiss,
    ) {
        KeyValueRow(stringResource(inherited.source.labelRes), Rupiah.format(inherited.amount))
        ToggleRow(
            title = stringResource(R.string.room_custom_fee_toggle),
            description = stringResource(R.string.room_custom_fee_inherit),
            checked = useCustom,
            onCheckedChange = { useCustom = it },
            modifier = Modifier.testTag("fee_custom_toggle"),
        )
        if (useCustom) {
            MoneyTextField(
                digits = digits,
                onDigitsChange = { digits = it },
                label = stringResource(R.string.room_custom_fee),
                isError = showErrors && error,
                supportingText = if (showErrors && error) stringResource(R.string.error_required) else null,
                modifier = Modifier.testTag("fee_input"),
            )
        }
        SekostButton(
            text = stringResource(R.string.action_save),
            icon = SekostIcons.Check,
            onClick = {
                showErrors = true
                if (!error) onSave(if (useCustom) Rupiah.parse(digits) else null)
            },
            modifier = Modifier.fillMaxWidth().padding(top = SekostTheme.spacing.sm).testTag("fee_save"),
        )
    }
}

/** Report a maintenance need or complaint. [roomPicker] is shown above the form when provided. */
@Composable
fun IssueSheet(
    onDismiss: () -> Unit,
    onSave: (IssueType, String, String) -> Unit,
    canSave: Boolean = true,
    roomPicker: (@Composable () -> Unit)? = null,
) {
    var type by rememberSaveable { mutableStateOf(IssueType.MAINTENANCE) }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val titleError = title.isBlank()

    SekostSheet(title = stringResource(R.string.issue_new), onDismiss = onDismiss) {
        roomPicker?.invoke()
        SegmentedPicker(
            segments = IssueType.entries.map { Segment(it, stringResource(it.labelRes), it.icon, it.tone) },
            selected = type,
            onSelect = { type = it },
        )
        SekostTextField(
            value = title,
            onValueChange = { title = it },
            label = stringResource(R.string.issue_title),
            placeholder = stringResource(R.string.issue_title_hint),
            isError = showErrors && titleError,
            supportingText = if (showErrors && titleError) stringResource(R.string.error_required) else null,
            modifier = Modifier.testTag("issue_title"),
        )
        SekostTextField(
            value = description,
            onValueChange = { description = it },
            label = stringResource(R.string.issue_description),
            singleLine = false,
            minLines = 3,
            modifier = Modifier.testTag("issue_description"),
        )
        SekostButton(
            text = stringResource(R.string.action_save),
            icon = SekostIcons.Check,
            tone = if (type == IssueType.COMPLAINT) ButtonTone.Accent else ButtonTone.Brand,
            onClick = {
                showErrors = true
                if (!titleError && canSave) onSave(type, title, description)
            },
            modifier = Modifier.fillMaxWidth().padding(top = SekostTheme.spacing.sm).testTag("issue_save"),
        )
    }
}
