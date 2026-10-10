package id.my.selo.sekost.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import id.my.selo.sekost.core.data.repository.SettingsRepository
import id.my.selo.sekost.core.model.AppSettings
import id.my.selo.sekost.core.model.ThemeMode
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            ownerName = prefs[Keys.OwnerName].orEmpty(),
            globalMonthlyFee = prefs[Keys.GlobalFee] ?: AppSettings.DEFAULT_GLOBAL_FEE,
            dueDay = prefs[Keys.DueDay] ?: AppSettings.DEFAULT_DUE_DAY,
            themeMode = prefs[Keys.Theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
        )
    }

    override suspend fun setOwnerName(name: String) = edit { it[Keys.OwnerName] = name.trim() }
    override suspend fun setGlobalMonthlyFee(fee: Long) = edit { it[Keys.GlobalFee] = fee.coerceAtLeast(0) }
    override suspend fun setDueDay(day: Int) = edit { it[Keys.DueDay] = day.coerceIn(1, 28) }
    override suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.Theme] = mode.name }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit(block)
    }

    private object Keys {
        val OwnerName = stringPreferencesKey("owner_name")
        val GlobalFee = longPreferencesKey("global_monthly_fee")
        val DueDay = intPreferencesKey("due_day")
        val Theme = stringPreferencesKey("theme_mode")
    }
}
