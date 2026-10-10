package id.my.selo.sekost.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.selo.sekost.core.common.Rupiah
import id.my.selo.sekost.core.data.repository.SampleDataSeeder
import id.my.selo.sekost.core.data.repository.SettingsRepository
import id.my.selo.sekost.core.model.AppSettings
import id.my.selo.sekost.core.model.ThemeMode
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** First-run flow: a welcome page, three setup steps, and a summary. */
enum class OnboardingStep {
    Welcome,
    Profile,
    Billing,
    Appearance,
    Ready,
    ;

    /** 1-based position among the setup steps, or `null` for Welcome/Ready. */
    val setupNumber: Int? get() = SETUP.indexOf(this).takeIf { it >= 0 }?.plus(1)

    companion object {
        val SETUP = listOf(Profile, Billing, Appearance)
    }
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Welcome,
    val ownerName: String = "",
    /** Digits only, as edited by `MoneyTextField`. */
    val feeDigits: String = AppSettings.DEFAULT_GLOBAL_FEE.toString(),
    val dueDay: Int = AppSettings.DEFAULT_DUE_DAY,
    /** Applied to the app immediately, so the user previews it live. */
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isSaving: Boolean = false,
    /** Settings are persisted; the screen should leave onboarding. */
    val isFinished: Boolean = false,
) {
    val globalFee: Long? get() = Rupiah.parse(feeDigits)?.takeIf { it > 0 }
    val canContinue: Boolean get() = !isSaving && (step != OnboardingStep.Billing || globalFee != null)
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val seeder: SampleDataSeeder,
) : ViewModel() {

    private val draft = MutableStateFlow(OnboardingUiState())

    val uiState: StateFlow<OnboardingUiState> = combine(draft, settings.settings) { form, appSettings ->
        form.copy(themeMode = appSettings.themeMode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), draft.value)

    init {
        // Start from whatever is already saved (e.g. restored from a backup) instead of hard defaults.
        viewModelScope.launch {
            val saved = settings.settings.first()
            draft.update {
                it.copy(ownerName = saved.ownerName, feeDigits = saved.globalMonthlyFee.toString(), dueDay = saved.dueDay)
            }
        }
    }

    fun next() = draft.update { state ->
        if (!state.canContinue) return@update state
        val steps = OnboardingStep.entries
        state.copy(step = steps[(state.step.ordinal + 1).coerceAtMost(steps.lastIndex)])
    }

    /** Goes one step back; returns `false` on the first step so the system can handle Back. */
    fun back(): Boolean {
        val current = draft.value.step
        if (current == OnboardingStep.Welcome) return false
        draft.update { it.copy(step = OnboardingStep.entries[current.ordinal - 1]) }
        return true
    }

    fun goTo(step: OnboardingStep) = draft.update { it.copy(step = step) }

    fun setOwnerName(name: String) = draft.update { it.copy(ownerName = name) }

    fun setFeeDigits(digits: String) = draft.update { it.copy(feeDigits = digits) }

    fun setDueDay(day: Int) = draft.update { it.copy(dueDay = day.coerceIn(1, 28)) }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    /**
     * Saves the setup (falling back to defaults for anything left invalid, e.g. when skipping) and
     * marks onboarding done. With [withSampleData], also seeds the demo portfolio.
     */
    fun finish(withSampleData: Boolean = false) {
        val form = draft.value
        if (form.isSaving || form.isFinished) return
        draft.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            if (withSampleData) seeder.seed()
            settings.completeOnboarding(
                ownerName = form.ownerName,
                globalMonthlyFee = form.globalFee ?: AppSettings.DEFAULT_GLOBAL_FEE,
                dueDay = form.dueDay,
            )
            draft.update { it.copy(isSaving = false, isFinished = true) }
        }
    }
}
