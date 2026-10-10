package id.my.selo.sekost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.selo.sekost.core.data.repository.PropertyRepository
import id.my.selo.sekost.core.data.repository.SettingsRepository
import id.my.selo.sekost.core.model.ThemeMode
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take

@HiltViewModel
class MainViewModel @Inject constructor(settings: SettingsRepository, properties: PropertyRepository) : ViewModel() {
    /** `null` until settings load — keeps the splash screen up so the theme never flashes. */
    val themeMode: StateFlow<ThemeMode?> = settings.settings
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Whether this launch starts in onboarding. Decided once (the start destination must not change
     * under a running NavHost); `null` until known. Installs that already hold properties — i.e.
     * people who used Sekost before onboarding existed — go straight to the dashboard.
     */
    val startWithOnboarding: StateFlow<Boolean?> = combine(settings.settings, properties.observeAll()) { appSettings, props ->
        !appSettings.onboardingCompleted && props.isEmpty()
    }
        .take(1)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
