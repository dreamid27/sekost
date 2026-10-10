package id.my.selo.sekost.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.selo.sekost.core.data.repository.SettingsRepository
import id.my.selo.sekost.core.model.AppSettings
import id.my.selo.sekost.core.model.ThemeMode
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setOwnerName(name: String) = launch { repository.setOwnerName(name) }

    fun setGlobalFee(fee: Long) = launch { repository.setGlobalMonthlyFee(fee) }

    fun setDueDay(day: Int) = launch { repository.setDueDay(day) }

    fun setTheme(mode: ThemeMode) = launch { repository.setThemeMode(mode) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
