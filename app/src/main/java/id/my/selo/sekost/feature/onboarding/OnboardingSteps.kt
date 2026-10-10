package id.my.selo.sekost.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import id.my.selo.sekost.R
import id.my.selo.sekost.core.common.Rupiah
import id.my.selo.sekost.core.designsystem.component.IconTile
import id.my.selo.sekost.core.designsystem.component.MoneyTextField
import id.my.selo.sekost.core.designsystem.component.SekostCard
import id.my.selo.sekost.core.designsystem.component.SekostTextField
import id.my.selo.sekost.core.designsystem.component.StatusDot
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.illustration.BrandMark
import id.my.selo.sekost.core.designsystem.illustration.CelebrationIllustration
import id.my.selo.sekost.core.designsystem.illustration.DayNightIllustration
import id.my.selo.sekost.core.designsystem.illustration.DueDateIllustration
import id.my.selo.sekost.core.designsystem.illustration.NameplateIllustration
import id.my.selo.sekost.core.designsystem.illustration.WelcomeIllustration
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.designsystem.theme.ToneColors
import id.my.selo.sekost.core.model.ThemeMode
import id.my.selo.sekost.core.ui.initialsOf
import id.my.selo.sekost.core.ui.labelRes

private val QUICK_FEES = listOf(500_000L, 750_000L, 1_000_000L, 1_500_000L, 2_000_000L)
private const val STAGGER_MS = 90

// ——— Welcome ———

@Composable
internal fun WelcomeStep() {
    val colors = SekostTheme.colors
    WelcomeIllustration(Modifier.padding(top = SekostTheme.spacing.sm))
    Spacer(Modifier.height(SekostTheme.spacing.lg))
    Text(
        stringResource(R.string.onboarding_welcome_overline).uppercase(),
        style = SekostTheme.typography.overline,
        color = colors.accent,
        modifier = Modifier.staggered(0),
    )
    Spacer(Modifier.height(SekostTheme.spacing.sm))
    Text(
        stringResource(R.string.onboarding_welcome_title),
        style = SekostTheme.typography.hero,
        color = colors.textPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.staggered(1),
    )
    Spacer(Modifier.height(SekostTheme.spacing.md))
    Text(
        stringResource(R.string.onboarding_welcome_body),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.staggered(2),
    )
    Spacer(Modifier.height(SekostTheme.spacing.xxl))
    SekostCard(Modifier.fillMaxWidth().staggered(3), contentPadding = PaddingValues(SekostTheme.spacing.xs)) {
        FeatureRow(SekostIcons.Door, colors.occupied, R.string.onboarding_feature_rooms_title, R.string.onboarding_feature_rooms_body, 4)
        FeatureRow(SekostIcons.Bill, colors.unpaid, R.string.onboarding_feature_billing_title, R.string.onboarding_feature_billing_body, 5)
        FeatureRow(
            SekostIcons.Toolbox,
            colors.maintenance,
            R.string.onboarding_feature_issues_title,
            R.string.onboarding_feature_issues_body,
            6
        )
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, tone: ToneColors, title: Int, body: Int, order: Int) {
    Row(
        Modifier.fillMaxWidth().padding(SekostTheme.spacing.md).staggered(order),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon, tone, size = SekostTheme.sizes.iconTile - SekostTheme.spacing.xs)
        Spacer(Modifier.width(SekostTheme.spacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xxs)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleSmall, color = SekostTheme.colors.textPrimary)
            Text(stringResource(body), style = MaterialTheme.typography.bodySmall, color = SekostTheme.colors.textSecondary)
        }
    }
}

// ——— Profile ———

@Composable
internal fun ProfileStep(ownerName: String, onOwnerName: (String) -> Unit) {
    val colors = SekostTheme.colors
    val name = ownerName.trim()
    NameplateIllustration(monogram = initialsOf(name).ifEmpty { stringResource(R.string.onboarding_plate_empty) })
    StepHeading(OnboardingStep.Profile, R.string.onboarding_profile_title, R.string.onboarding_profile_body)
    SekostTextField(
        value = ownerName,
        onValueChange = onOwnerName,
        label = stringResource(R.string.settings_owner),
        placeholder = stringResource(R.string.onboarding_profile_hint),
        leadingIcon = SekostIcons.Person,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        modifier = Modifier.testTag("onboarding_owner"),
    )
    Spacer(Modifier.height(SekostTheme.spacing.lg))
    SekostCard(Modifier.fillMaxWidth(), color = colors.surface) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandMark(size = SekostTheme.sizes.iconTile)
            Spacer(Modifier.width(SekostTheme.spacing.md))
            Column(Modifier.animateContentSize(tween(SekostTheme.motion.medium))) {
                Text(
                    stringResource(R.string.onboarding_profile_preview).uppercase(),
                    style = SekostTheme.typography.overline,
                    color = colors.textTertiary,
                )
                Text(
                    if (name.isEmpty()) {
                        stringResource(R.string.onboarding_greeting_anonymous)
                    } else {
                        stringResource(R.string.onboarding_greeting, name)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary,
                    maxLines = 2,
                    modifier = Modifier.testTag("onboarding_greeting"),
                )
            }
        }
    }
}

// ——— Billing ———

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BillingStep(feeDigits: String, fee: Long?, dueDay: Int, onFeeDigits: (String) -> Unit, onDueDay: (Int) -> Unit) {
    val colors = SekostTheme.colors
    DueDateIllustration(dueDay)
    StepHeading(OnboardingStep.Billing, R.string.onboarding_billing_title, R.string.onboarding_billing_body)
    MoneyTextField(
        digits = feeDigits,
        onDigitsChange = onFeeDigits,
        label = stringResource(R.string.onboarding_fee_label),
        isError = fee == null,
        supportingText = if (fee == null) stringResource(R.string.error_required) else null,
        modifier = Modifier.testTag("onboarding_fee"),
    )
    Spacer(Modifier.height(SekostTheme.spacing.md))
    Text(
        stringResource(R.string.onboarding_fee_quick),
        style = MaterialTheme.typography.labelMedium,
        color = colors.textSecondary,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(SekostTheme.spacing.sm))
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SekostTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.sm),
    ) {
        QUICK_FEES.forEach { amount ->
            FeeChip(Rupiah.compact(amount), selected = fee == amount, onClick = { onFeeDigits(amount.toString()) })
        }
    }
    Spacer(Modifier.height(SekostTheme.spacing.xl))
    SekostCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(SekostIcons.Calendar, colors.maintenance, size = SekostTheme.sizes.iconTile - SekostTheme.spacing.xs)
            Spacer(Modifier.width(SekostTheme.spacing.md))
            Column(verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xxs)) {
                Text(stringResource(R.string.settings_due_day), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                Text(
                    stringResource(R.string.settings_due_day_value, dueDay),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.testTag("onboarding_due_day_value"),
                )
            }
        }
        Spacer(Modifier.height(SekostTheme.spacing.md))
        DayGrid(dueDay, onDueDay, Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(SekostTheme.spacing.sm))
        Text(stringResource(R.string.settings_due_day_desc), style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
    }
}

@Composable
private fun FeeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = SekostTheme.colors
    val container by animateColorAsState(if (selected) colors.brandContainer else colors.surfaceRaised, label = "chip-bg")
    val border by animateColorAsState(if (selected) colors.brand else colors.outline, label = "chip-border")
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(SekostTheme.radii.pill),
        color = container,
        border = androidx.compose.foundation.BorderStroke(SekostTheme.sizes.hairline, border),
        modifier = Modifier.height(SekostTheme.sizes.chipHeight + SekostTheme.spacing.xs).testTag("onboarding_fee_chip"),
    ) {
        Box(Modifier.padding(horizontal = SekostTheme.spacing.md), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) colors.onBrandContainer else colors.textPrimary)
        }
    }
}

/** 1–28 as four 7-day weeks; days before the due day are tinted to show the "grace" window. */
@Composable
private fun DayGrid(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = SekostTheme.colors
    Column(
        modifier.widthIn(max = SekostTheme.sizes.dayCell * 7).fillMaxWidth().selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xxs),
    ) {
        (0 until 4).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                (1..7).forEach { weekday ->
                    val day = week * 7 + weekday
                    val isSelected = day == selected
                    val background by animateColorAsState(
                        when {
                            isSelected -> colors.brand
                            day < selected -> colors.brandContainer.copy(alpha = 0.55f)
                            else -> colors.surfaceRaised.copy(alpha = 0f)
                        },
                        tween(SekostTheme.motion.medium),
                        label = "day-bg",
                    )
                    val content by animateColorAsState(
                        if (isSelected) colors.onBrand else colors.textPrimary,
                        tween(SekostTheme.motion.medium),
                        label = "day-fg",
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(SekostTheme.spacing.xxs)
                            .clip(CircleShape)
                            .background(background)
                            .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(day) })
                            .testTag("onboarding_day_$day"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            day.toString(),
                            style = if (isSelected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
                            color = content,
                        )
                    }
                }
            }
        }
    }
}

// ——— Appearance ———

@Composable
internal fun AppearanceStep(themeMode: ThemeMode, onTheme: (ThemeMode) -> Unit) {
    val night = when (themeMode) {
        ThemeMode.LIGHT -> 0f
        ThemeMode.DARK -> 1f
        ThemeMode.SYSTEM -> 0.5f
    }
    DayNightIllustration(night)
    StepHeading(OnboardingStep.Appearance, R.string.onboarding_appearance_title, R.string.onboarding_appearance_body)
    Row(
        Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(SekostTheme.spacing.md),
    ) {
        listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM).forEach { mode ->
            ThemeOption(mode, selected = mode == themeMode, onClick = { onTheme(mode) }, modifier = Modifier.weight(1f))
        }
    }
}

private val LowerRightHalf = GenericShape { size, _ ->
    moveTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}

@Composable
private fun ThemeOption(mode: ThemeMode, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SekostTheme.colors
    val shape = RoundedCornerShape(SekostTheme.radii.md)
    val border by animateColorAsState(
        if (selected) colors.brand else colors.outline,
        tween(SekostTheme.motion.medium),
        label = "theme-border"
    )
    val label by animateColorAsState(
        if (selected) colors.brand else colors.textSecondary,
        tween(SekostTheme.motion.medium),
        label = "theme-label"
    )
    val badgeScale by animateFloatAsState(
        if (selected) 1f else 0f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "theme-check",
    )
    Column(
        modifier
            .clip(RoundedCornerShape(SekostTheme.radii.lg))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(SekostTheme.spacing.xxs)
            .testTag("onboarding_theme_${mode.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(SekostTheme.sizes.themePreview)
                .border(SekostTheme.sizes.hairline * 2, border, shape)
                .padding(SekostTheme.spacing.xs)
                .clip(RoundedCornerShape(SekostTheme.radii.sm)),
        ) {
            when (mode) {
                ThemeMode.LIGHT -> SekostTheme(darkTheme = false) { MiniApp() }
                ThemeMode.DARK -> SekostTheme(darkTheme = true) { MiniApp() }
                ThemeMode.SYSTEM -> {
                    SekostTheme(darkTheme = false) { MiniApp() }
                    Box(Modifier.fillMaxSize().clip(LowerRightHalf)) { SekostTheme(darkTheme = true) { MiniApp() } }
                }
            }
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(SekostTheme.spacing.xs)
                    .graphicsLayer {
                        scaleX = badgeScale
                        scaleY = badgeScale
                        alpha = badgeScale.coerceIn(0f, 1f)
                    }
                    .size(SekostTheme.sizes.iconLg)
                    .clip(CircleShape)
                    .background(colors.brand),
                contentAlignment = Alignment.Center,
            ) {
                Icon(SekostIcons.Check, null, tint = colors.onBrand, modifier = Modifier.size(SekostTheme.sizes.iconSm))
            }
        }
        Spacer(Modifier.height(SekostTheme.spacing.sm))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(mode.icon, null, tint = label, modifier = Modifier.size(SekostTheme.sizes.iconSm))
            Spacer(Modifier.width(SekostTheme.spacing.xs))
            Text(stringResource(mode.labelRes), style = MaterialTheme.typography.labelLarge, color = label)
        }
    }
}

/** A tiny home screen drawn with whatever theme is current — used to preview light/dark. */
@Composable
private fun MiniApp() {
    val colors = SekostTheme.colors
    val line = RoundedCornerShape(SekostTheme.radii.pill)
    Column(
        Modifier.fillMaxSize().background(colors.background).padding(SekostTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xs + SekostTheme.spacing.xxs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandMark(size = SekostTheme.sizes.iconSm)
            Spacer(Modifier.width(SekostTheme.spacing.xs))
            Box(
                Modifier.fillMaxWidth(
                    0.6f
                ).height(SekostTheme.spacing.xs + SekostTheme.spacing.xxs).clip(line).background(colors.textPrimary)
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(SekostTheme.radii.xs))
                .background(Brush.linearGradient(listOf(colors.heroGradientStart, colors.heroGradientEnd)))
                .padding(SekostTheme.spacing.xs + SekostTheme.spacing.xxs),
            verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xs),
        ) {
            Box(Modifier.fillMaxWidth(0.45f).height(SekostTheme.spacing.xs).clip(line).background(colors.onHero.copy(alpha = 0.6f)))
            Box(Modifier.fillMaxWidth(0.7f).height(SekostTheme.spacing.sm).clip(line).background(colors.onHero))
            Box(Modifier.fillMaxWidth().height(SekostTheme.spacing.xs).clip(line).background(colors.onHero.copy(alpha = 0.16f))) {
                Box(Modifier.fillMaxWidth(0.65f).fillMaxHeight().clip(line).background(colors.accent))
            }
        }
        listOf(colors.occupied, colors.vacant, colors.maintenance).forEach { tone ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(SekostTheme.radii.xs))
                    .background(colors.surfaceRaised)
                    .padding(SekostTheme.spacing.xs + SekostTheme.spacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusDot(tone.content)
                Spacer(Modifier.width(SekostTheme.spacing.xs))
                Box(
                    Modifier.fillMaxWidth(
                        0.7f
                    ).height(SekostTheme.spacing.xs).clip(line).background(colors.textSecondary.copy(alpha = 0.5f))
                )
            }
        }
    }
}

private val ThemeMode.icon: ImageVector
    get() = when (this) {
        ThemeMode.LIGHT -> SekostIcons.Sun
        ThemeMode.DARK -> SekostIcons.Moon
        ThemeMode.SYSTEM -> SekostIcons.Device
    }

// ——— Ready ———

@Composable
internal fun ReadyStep(state: OnboardingUiState, onEdit: (OnboardingStep) -> Unit) {
    val colors = SekostTheme.colors
    val name = state.ownerName.trim()
    CelebrationIllustration()
    Spacer(Modifier.height(SekostTheme.spacing.md))
    Text(
        if (name.isEmpty()) {
            stringResource(
                R.string.onboarding_ready_title
            )
        } else {
            stringResource(R.string.onboarding_ready_title_named, name)
        },
        style = MaterialTheme.typography.headlineLarge,
        color = colors.textPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.staggered(0).testTag("onboarding_ready_title"),
    )
    Spacer(Modifier.height(SekostTheme.spacing.sm))
    Text(
        stringResource(R.string.onboarding_ready_body),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.staggered(1),
    )
    Spacer(Modifier.height(SekostTheme.spacing.xl))
    SekostCard(Modifier.fillMaxWidth().staggered(2), contentPadding = PaddingValues(SekostTheme.spacing.xs)) {
        SummaryRow(
            icon = SekostIcons.Person,
            tone = ToneColors(colors.accent, colors.accentContainer, colors.onAccentContainer),
            label = stringResource(R.string.settings_owner),
            value = name.ifEmpty { stringResource(R.string.settings_owner_empty) },
            onClick = { onEdit(OnboardingStep.Profile) },
        )
        SummaryRow(
            icon = SekostIcons.Wallet,
            tone = ToneColors(colors.brand, colors.brandContainer, colors.onBrandContainer),
            label = stringResource(R.string.settings_global_fee),
            value = "${Rupiah.format(state.globalFee ?: 0)} ${stringResource(R.string.per_month)}",
            onClick = { onEdit(OnboardingStep.Billing) },
        )
        SummaryRow(
            icon = SekostIcons.Calendar,
            tone = colors.maintenance,
            label = stringResource(R.string.settings_due_day),
            value = stringResource(R.string.settings_due_day_value, state.dueDay),
            onClick = { onEdit(OnboardingStep.Billing) },
        )
        SummaryRow(
            icon = state.themeMode.icon,
            tone = colors.vacant,
            label = stringResource(R.string.settings_theme),
            value = stringResource(state.themeMode.labelRes),
            onClick = { onEdit(OnboardingStep.Appearance) },
        )
    }
}

@Composable
private fun SummaryRow(icon: ImageVector, tone: ToneColors, label: String, value: String, onClick: () -> Unit) {
    val colors = SekostTheme.colors
    Surface(onClick = onClick, color = colors.surfaceRaised, shape = MaterialTheme.shapes.large) {
        Row(
            Modifier.fillMaxWidth().padding(
                horizontal = SekostTheme.spacing.md,
                vertical = SekostTheme.spacing.sm + SekostTheme.spacing.xxs
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(icon, tone, size = SekostTheme.sizes.iconTile - SekostTheme.spacing.sm)
            Spacer(Modifier.width(SekostTheme.spacing.md))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                Text(value, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
            }
            Icon(
                SekostIcons.Edit,
                stringResource(R.string.onboarding_edit, label),
                tint = colors.textTertiary,
                modifier = Modifier.size(SekostTheme.sizes.iconSm),
            )
        }
    }
}

// ——— shared ———

@Composable
private fun StepHeading(step: OnboardingStep, title: Int, body: Int) {
    val colors = SekostTheme.colors
    Spacer(Modifier.height(SekostTheme.spacing.md))
    step.setupNumber?.let { number ->
        Text(
            stringResource(R.string.onboarding_step, number, OnboardingStep.SETUP.size).uppercase(),
            style = SekostTheme.typography.overline,
            color = colors.accent,
            modifier = Modifier.staggered(0),
        )
        Spacer(Modifier.height(SekostTheme.spacing.xs))
    }
    Text(
        stringResource(title),
        style = MaterialTheme.typography.headlineLarge,
        color = colors.textPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.staggered(1),
    )
    Spacer(Modifier.height(SekostTheme.spacing.sm))
    Text(
        stringResource(body),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.staggered(2),
    )
    Spacer(Modifier.height(SekostTheme.spacing.xl))
}

/** Fades and lifts content in, delayed by its [order] so a page assembles top to bottom. */
@Composable
private fun Modifier.staggered(order: Int): Modifier {
    val progress = remember { Animatable(0f) }
    val delay = order * STAGGER_MS
    val duration = SekostTheme.motion.slow
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(duration, delayMillis = delay, easing = FastOutSlowInEasing)) }
    val lift = with(LocalDensity.current) { SekostTheme.spacing.lg.toPx() }
    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * lift
    }
}
