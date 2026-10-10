package id.my.selo.sekost.feature.property

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import id.my.selo.sekost.R
import id.my.selo.sekost.core.common.Rupiah
import id.my.selo.sekost.core.designsystem.component.MoneyTextField
import id.my.selo.sekost.core.designsystem.component.Segment
import id.my.selo.sekost.core.designsystem.component.SegmentedPicker
import id.my.selo.sekost.core.designsystem.component.SekostButton
import id.my.selo.sekost.core.designsystem.component.SekostSheet
import id.my.selo.sekost.core.designsystem.component.SekostTextField
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.PropertyType
import id.my.selo.sekost.core.ui.icon
import id.my.selo.sekost.core.ui.labelRes

/** Create (when [initial] is null) or edit a property. */
@Composable
fun PropertyEditorSheet(
    initial: Property?,
    globalFee: Long,
    onDismiss: () -> Unit,
    onSave: (Property) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var address by rememberSaveable { mutableStateOf(initial?.address.orEmpty()) }
    var type by rememberSaveable { mutableStateOf(initial?.type ?: PropertyType.KOST) }
    var useDefaultFee by rememberSaveable { mutableStateOf(initial?.defaultMonthlyFee != null) }
    var feeDigits by rememberSaveable { mutableStateOf(initial?.defaultMonthlyFee?.toString().orEmpty()) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val nameError = name.isBlank()
    val feeError = useDefaultFee && Rupiah.parse(feeDigits) == null

    SekostSheet(
        title = stringResource(if (initial == null) R.string.property_new else R.string.property_edit),
        onDismiss = onDismiss,
    ) {
        SegmentedPicker(
            segments = PropertyType.entries.map { Segment(it, stringResource(it.labelRes), it.icon) },
            selected = type,
            onSelect = { type = it },
        )
        SekostTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.property_name),
            placeholder = stringResource(R.string.property_name_hint),
            leadingIcon = SekostIcons.Tag,
            isError = showErrors && nameError,
            supportingText = if (showErrors && nameError) stringResource(R.string.error_required) else null,
            modifier = Modifier.testTag("property_name"),
        )
        SekostTextField(
            value = address,
            onValueChange = { address = it },
            label = stringResource(R.string.property_address),
            leadingIcon = SekostIcons.Home,
            modifier = Modifier.testTag("property_address"),
        )
        ToggleRow(
            title = stringResource(R.string.property_use_default_fee),
            description = stringResource(R.string.property_use_default_fee_desc, Rupiah.format(globalFee)),
            checked = useDefaultFee,
            onCheckedChange = { useDefaultFee = it },
        )
        if (useDefaultFee) {
            MoneyTextField(
                digits = feeDigits,
                onDigitsChange = { feeDigits = it },
                label = stringResource(R.string.property_default_fee),
                isError = showErrors && feeError,
                supportingText = if (showErrors && feeError) stringResource(R.string.error_required) else null,
                modifier = Modifier.testTag("property_fee"),
            )
        }
        SekostButton(
            text = stringResource(R.string.action_save),
            onClick = {
                showErrors = true
                if (!nameError && !feeError) {
                    onSave(
                        Property(
                            id = initial?.id ?: 0,
                            name = name,
                            type = type,
                            address = address,
                            defaultMonthlyFee = if (useDefaultFee) Rupiah.parse(feeDigits) else null,
                        ),
                    )
                }
            },
            icon = SekostIcons.Check,
            modifier = Modifier.fillMaxWidth().padding(top = SekostTheme.spacing.sm).testTag("property_save"),
        )
    }
}

@Composable
fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth().padding(vertical = SekostTheme.spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xxs)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = SekostTheme.colors.textPrimary)
            Text(description, style = MaterialTheme.typography.bodySmall, color = SekostTheme.colors.textSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = SekostTheme.spacing.md),
            colors = SwitchDefaults.colors(
                checkedTrackColor = SekostTheme.colors.brand,
                checkedThumbColor = SekostTheme.colors.onBrand,
                uncheckedTrackColor = SekostTheme.colors.surfaceSunken,
                uncheckedBorderColor = SekostTheme.colors.outlineStrong,
                uncheckedThumbColor = SekostTheme.colors.textTertiary,
            ),
        )
    }
}
