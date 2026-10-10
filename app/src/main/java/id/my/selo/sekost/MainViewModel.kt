package id.my.selo.sekost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.selo.sekost.core.data.repository.SettingsRepository
import id.my.selo.sekost.core.model.ThemeMode
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(settings: SettingsRepository) : ViewModel() {
    /** `null` until settings load — keeps the splash screen up so the theme never flashes. */
    val themeMode: StateFlow<ThemeMode?> = settings.settings
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
