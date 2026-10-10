package id.my.selo.sekost.feature.settings

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.selo.sekost.BuildConfig
import id.my.selo.sekost.R
import id.my.selo.sekost.core.common.Rupiah
import id.my.selo.sekost.core.designsystem.component.IconTile
import id.my.selo.sekost.core.designsystem.component.MoneyTextField
import id.my.selo.sekost.core.designsystem.component.SectionHeader
import id.my.selo.sekost.core.designsystem.component.Segment
import id.my.selo.sekost.core.designsystem.component.SegmentedPicker
import id.my.selo.sekost.core.designsystem.component.SekostButton
import id.my.selo.sekost.core.designsystem.component.SekostCard
import id.my.selo.sekost.core.designsystem.component.SekostSheet
import id.my.selo.sekost.core.designsystem.component.SekostTextField
import id.my.selo.sekost.core.designsystem.component.SekostTopBar
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.illustration.BrandMark
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.designsystem.theme.ToneColors
import id.my.selo.sekost.core.model.AppSettings
import id.my.selo.sekost.core.model.ThemeMode
import id.my.selo.sekost.core.ui.labelRes

@Composable
fun SettingsRoute(viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    settings?.let {
        SettingsScreen(
            settings = it,
            onOwnerName = viewModel::setOwnerName,
            onGlobalFee = viewModel::setGlobalFee,
            onDueDay = viewModel::setDueDay,
            onTheme = viewModel::setTheme,
        )
    }
}

private enum class SettingsSheet { None, Owner, Fee }

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onOwnerName: (String) -> Unit,
    onGlobalFee: (Long) -> Unit,
    onDueDay: (Int) -> Unit,
    onTheme: (ThemeMode) -> Unit,
) {
    var sheet by rememberSaveable { mutableStateOf(SettingsSheet.None) }
    val colors = SekostTheme.colors
    val gutter = SekostTheme.spacing.screenGutter
    val brandTone = ToneColors(colors.brand, colors.brandContainer, colors.onBrandContainer)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = SekostTheme.spacing.huge)
            .testTag("settings"),
    ) {
        SekostTopBar(title = stringResource(R.string.settings_title), overline = stringResource(R.string.settings_overline))
        Column(Modifier.padding(horizontal = gutter)) {
            SectionHeader(stringResource(R.string.settings_profile))
            SekostCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(SekostTheme.spacing.xs)) {
                SettingRow(
                    icon = SekostIcons.Person,
                    tone = ToneColors(colors.accent, colors.accentContainer, colors.onAccentContainer),
                    title = stringResource(R.string.settings_owner),
                    value = settings.ownerName.ifBlank { stringResource(R.string.settings_owner_empty) },
                    onClick = { sheet = SettingsSheet.Owner },
                    modifier = Modifier.testTag("settings_owner"),
                )
            }

            SectionHeader(stringResource(R.string.settings_billing_section))
            SekostCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(SekostTheme.spacing.xs)) {
                SettingRow(
                    icon = SekostIcons.Wallet,
                    tone = brandTone,
                    title = stringResource(R.string.settings_global_fee),
                    value = "${Rupiah.format(settings.globalMonthlyFee)} ${stringResource(R.string.per_month)}",
                    description = stringResource(R.string.settings_global_fee_desc),
                    onClick = { sheet = SettingsSheet.Fee },
                    modifier = Modifier.testTag("settings_global_fee"),
                )
                HorizontalDivider(color = colors.outline, modifier = Modifier.padding(horizontal = SekostTheme.spacing.md))
                DueDayRow(settings.dueDay, onDueDay)
            }

            SectionHeader(stringResource(R.string.settings_appearance))
            SekostCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(SekostIcons.Moon, colors.vacant, size = SekostTheme.sizes.iconTile - SekostTheme.spacing.xs)
                    Spacer(Modifier.width(SekostTheme.spacing.md))
                    Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                }
                Spacer(Modifier.height(SekostTheme.spacing.md))
                SegmentedPicker(
                    segments = ThemeMode.entries.map { Segment(it, stringResource(it.labelRes)) },
                    selected = settings.themeMode,
                    onSelect = onTheme,
                    modifier = Modifier.testTag("settings_theme"),
                )
            }

            Column(
                Modifier.fillMaxWidth().padding(top = SekostTheme.spacing.huge),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BrandMark(size = SekostTheme.sizes.iconTile)
                Spacer(Modifier.height(SekostTheme.spacing.sm))
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
                Text(
                    stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
            }
        }
    }

    when (sheet) {
        SettingsSheet.None -> Unit
        SettingsSheet.Owner -> {
            var name by rememberSaveable { mutableStateOf(settings.ownerName) }
            SekostSheet(
                title = stringResource(R.string.settings_owner),
                subtitle = stringResource(R.string.settings_owner_hint),
                onDismiss = { sheet = SettingsSheet.None },
            ) {
                SekostTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.settings_owner),
                    leadingIcon = SekostIcons.Person,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    modifier = Modifier.testTag("owner_input"),
                )
                SekostButton(
                    stringResource(R.string.action_save),
                    onClick = {
                        onOwnerName(name)
                        sheet = SettingsSheet.None
                    },
                    icon = SekostIcons.Check,
                    modifier = Modifier.fillMaxWidth().testTag("owner_save"),
                )
            }
        }
        SettingsSheet.Fee -> {
            var digits by rememberSaveable { mutableStateOf(settings.globalMonthlyFee.toString()) }
            val error = Rupiah.parse(digits) == null
            SekostSheet(
                title = stringResource(R.string.settings_global_fee),
                subtitle = stringResource(R.string.settings_global_fee_desc),
                onDismiss = { sheet = SettingsSheet.None },
            ) {
                MoneyTextField(
                    digits = digits,
                    onDigitsChange = { digits = it },
                    label = stringResource(R.string.settings_global_fee),
                    isError = error,
                    supportingText = if (error) stringResource(R.string.error_required) else null,
                    modifier = Modifier.testTag("global_fee_input"),
                )
                SekostButton(
                    stringResource(R.string.action_save),
                    enabled = !error,
                    onClick = {
                        Rupiah.parse(digits)?.let(onGlobalFee)
                        sheet = SettingsSheet.None
                    },
                    icon = SekostIcons.Check,
                    modifier = Modifier.fillMaxWidth().testTag("global_fee_save"),
                )
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    tone: ToneColors,
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    val colors = SekostTheme.colors
    androidx.compose.material3.Surface(
        onClick = onClick,
        color = colors.surfaceRaised,
        shape = MaterialTheme.shapes.large,
        modifier = modifier,
    ) {
        Row(Modifier.fillMaxWidth().padding(SekostTheme.spacing.md), verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon, tone, size = SekostTheme.sizes.iconTile - SekostTheme.spacing.xs)
            Spacer(Modifier.width(SekostTheme.spacing.md))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xxs)) {
                Text(title, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                Text(value, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                if (description != null) {
                    Text(description, style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
                }
            }
            Icon(SekostIcons.ChevronRight, null, tint = colors.textTertiary, modifier = Modifier.size(SekostTheme.sizes.iconMd))
        }
    }
}

@Composable
private fun DueDayRow(day: Int, onChange: (Int) -> Unit) {
    val colors = SekostTheme.colors
    val stepper = IconButtonDefaults.iconButtonColors(containerColor = colors.surfaceSunken, contentColor = colors.textPrimary)
    Row(Modifier.fillMaxWidth().padding(SekostTheme.spacing.md), verticalAlignment = Alignment.CenterVertically) {
        IconTile(SekostIcons.Calendar, colors.maintenance, size = SekostTheme.sizes.iconTile - SekostTheme.spacing.xs)
        Spacer(Modifier.width(SekostTheme.spacing.md))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xxs)) {
            Text(stringResource(R.string.settings_due_day), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            Text(
                stringResource(R.string.settings_due_day_value, day),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier.testTag("due_day_value"),
            )
            Text(stringResource(R.string.settings_due_day_desc), style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
        }
        IconButton(onClick = { onChange(day - 1) }, enabled = day > 1, colors = stepper, modifier = Modifier.testTag("due_day_minus")) {
            Icon(SekostIcons.Minus, stringResource(R.string.settings_decrease), modifier = Modifier.size(SekostTheme.sizes.iconMd))
        }
        Spacer(Modifier.width(SekostTheme.spacing.xs))
        IconButton(onClick = { onChange(day + 1) }, enabled = day < 28, colors = stepper, modifier = Modifier.testTag("due_day_plus")) {
            Icon(SekostIcons.Plus, stringResource(R.string.settings_increase), modifier = Modifier.size(SekostTheme.sizes.iconMd))
        }
    }
}
