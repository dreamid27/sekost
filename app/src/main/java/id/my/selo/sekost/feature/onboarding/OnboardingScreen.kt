package id.my.selo.sekost.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.selo.sekost.R
import id.my.selo.sekost.core.designsystem.component.ProgressTrack
import id.my.selo.sekost.core.designsystem.component.SekostButton
import id.my.selo.sekost.core.designsystem.component.SekostOutlinedButton
import id.my.selo.sekost.core.designsystem.component.SekostTextButton
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.illustration.BrandMark
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.model.ThemeMode

@Composable
fun OnboardingRoute(onFinished: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isFinished) { if (state.isFinished) onFinished() }
    BackHandler(enabled = state.step != OnboardingStep.Welcome) { viewModel.back() }
    OnboardingScreen(
        state = state,
        onNext = viewModel::next,
        onBack = { viewModel.back() },
        onGoTo = viewModel::goTo,
        onSkip = { viewModel.finish() },
        onOwnerName = viewModel::setOwnerName,
        onFeeDigits = viewModel::setFeeDigits,
        onDueDay = viewModel::setDueDay,
        onTheme = viewModel::setTheme,
        onFinish = { viewModel.finish() },
        onExploreSample = { viewModel.finish(withSampleData = true) },
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onGoTo: (OnboardingStep) -> Unit,
    onSkip: () -> Unit,
    onOwnerName: (String) -> Unit,
    onFeeDigits: (String) -> Unit,
    onDueDay: (Int) -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onFinish: () -> Unit,
    onExploreSample: () -> Unit,
) {
    val gutter = SekostTheme.spacing.screenGutter
    val duration = SekostTheme.motion.slow
    Column(
        Modifier
            .fillMaxSize()
            .background(SekostTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("onboarding"),
    ) {
        OnboardingTopBar(state.step, enabled = !state.isSaving, onBack = onBack, onSkip = onSkip)
        AnimatedContent(
            targetState = state.step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                val enter = slideInHorizontally(tween(duration)) { if (forward) it / 4 else -it / 4 } + fadeIn(tween(duration))
                val exit = slideOutHorizontally(tween(duration)) { if (forward) -it / 4 else it / 4 } + fadeOut(tween(duration / 2))
                (enter togetherWith exit).using(SizeTransform(clip = false))
            },
            label = "onboarding-step",
        ) { step ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = gutter)
                    .padding(bottom = SekostTheme.spacing.xl)
                    .testTag("onboarding_step_${step.name.lowercase()}"),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (step) {
                    OnboardingStep.Welcome -> WelcomeStep()
                    OnboardingStep.Profile -> ProfileStep(state.ownerName, onOwnerName)
                    OnboardingStep.Billing -> BillingStep(state.feeDigits, state.globalFee, state.dueDay, onFeeDigits, onDueDay)
                    OnboardingStep.Appearance -> AppearanceStep(state.themeMode, onTheme)
                    OnboardingStep.Ready -> ReadyStep(state, onEdit = onGoTo)
                }
            }
        }
        BottomActions(state, onNext = onNext, onFinish = onFinish, onExploreSample = onExploreSample)
    }
}

@Composable
private fun OnboardingTopBar(step: OnboardingStep, enabled: Boolean, onBack: () -> Unit, onSkip: () -> Unit) {
    val colors = SekostTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(SekostTheme.sizes.buttonHeight + SekostTheme.spacing.sm)
            .padding(horizontal = SekostTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(SekostTheme.sizes.buttonHeight), contentAlignment = Alignment.Center) {
            if (step == OnboardingStep.Welcome) {
                BrandMark(size = SekostTheme.sizes.iconTile - SekostTheme.spacing.sm)
            } else {
                IconButton(onClick = onBack, enabled = enabled, modifier = Modifier.testTag("onboarding_back")) {
                    Icon(SekostIcons.Back, stringResource(R.string.action_back), tint = colors.textPrimary)
                }
            }
        }
        Box(Modifier.weight(1f).padding(horizontal = SekostTheme.spacing.md), contentAlignment = Alignment.Center) {
            val number = step.setupNumber
            when {
                number != null -> StepProgress(number)
                step == OnboardingStep.Welcome -> Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Box(Modifier.width(SekostTheme.sizes.buttonHeight * 2), contentAlignment = Alignment.CenterEnd) {
            if (step != OnboardingStep.Ready) {
                SekostTextButton(
                    stringResource(R.string.onboarding_skip),
                    onClick = { if (enabled) onSkip() },
                    contentColor = colors.textSecondary,
                    modifier = Modifier.testTag("onboarding_skip"),
                )
            }
        }
    }
}

/** Three pill segments; the current one fills in as you arrive. */
@Composable
private fun StepProgress(number: Int) {
    val colors = SekostTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xs)) {
        OnboardingStep.SETUP.indices.forEach { i ->
            ProgressTrack(
                fraction = if (i < number) 1f else 0f,
                color = colors.brand,
                trackColor = colors.surfaceSunken,
                height = SekostTheme.sizes.stepIndicator,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BottomActions(state: OnboardingUiState, onNext: () -> Unit, onFinish: () -> Unit, onExploreSample: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = SekostTheme.spacing.screenGutter)
            .padding(top = SekostTheme.spacing.sm, bottom = SekostTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.sm),
    ) {
        when (state.step) {
            OnboardingStep.Ready -> {
                SekostButton(
                    stringResource(R.string.onboarding_ready_finish),
                    onClick = onFinish,
                    enabled = !state.isSaving,
                    icon = SekostIcons.Check,
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_finish"),
                )
                SekostOutlinedButton(
                    stringResource(R.string.onboarding_ready_sample),
                    onClick = onExploreSample,
                    enabled = !state.isSaving,
                    icon = SekostIcons.Spark,
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_sample"),
                )
                Text(
                    stringResource(R.string.onboarding_ready_sample_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = SekostTheme.colors.textTertiary,
                    textAlign = TextAlign.Center,
                )
            }
            else -> {
                val welcome = state.step == OnboardingStep.Welcome
                SekostButton(
                    stringResource(if (welcome) R.string.onboarding_start else R.string.onboarding_next),
                    onClick = onNext,
                    enabled = state.canContinue,
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_next"),
                )
                if (welcome) {
                    Text(
                        stringResource(R.string.onboarding_welcome_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = SekostTheme.colors.textTertiary,
                    )
                }
            }
        }
    }
}
