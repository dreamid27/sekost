package id.my.selo.sekost.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.selo.sekost.core.data.repository.PropertyRepository
import id.my.selo.sekost.core.data.repository.SampleDataSeeder
import id.my.selo.sekost.core.data.repository.SettingsRepository
import id.my.selo.sekost.core.domain.ObserveRoomOverviewsUseCase
import id.my.selo.sekost.core.domain.PortfolioSummary
import id.my.selo.sekost.core.domain.TimeProvider
import id.my.selo.sekost.core.model.Property
import java.time.LocalTime
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PropertyCard(val property: Property, val summary: PortfolioSummary)

enum class DayPart { MORNING, AFTERNOON, EVENING, NIGHT }

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(
        val ownerName: String,
        val dayPart: DayPart,
        val period: YearMonth,
        val globalFee: Long,
        val summary: PortfolioSummary,
        val properties: List<PropertyCard>,
    ) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeRoomOverviews: ObserveRoomOverviewsUseCase,
    private val properties: PropertyRepository,
    settings: SettingsRepository,
    private val seeder: SampleDataSeeder,
    time: TimeProvider,
) : ViewModel() {

    private val period = time.currentPeriod()

    val uiState: StateFlow<HomeUiState> = combine(
        observeRoomOverviews(period),
        properties.observeAll(),
        settings.settings,
    ) { overviews, props, appSettings ->
        val byProperty = overviews.groupBy { it.room.propertyId }
        HomeUiState.Ready(
            ownerName = appSettings.ownerName,
            dayPart = dayPartOf(LocalTime.now()),
            period = period,
            globalFee = appSettings.globalMonthlyFee,
            summary = PortfolioSummary.from(overviews),
            properties = props.map { PropertyCard(it, PortfolioSummary.from(byProperty[it.id].orEmpty())) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    fun saveProperty(property: Property) {
        viewModelScope.launch { properties.save(property) }
    }

    fun loadSampleData() {
        viewModelScope.launch { seeder.seed() }
    }

    companion object {
        fun dayPartOf(time: LocalTime): DayPart = when (time.hour) {
            in 4..10 -> DayPart.MORNING
            in 11..14 -> DayPart.AFTERNOON
            in 15..17 -> DayPart.EVENING
            else -> DayPart.NIGHT
        }
    }
}
