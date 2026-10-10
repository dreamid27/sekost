package id.my.selo.sekost.feature.property

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.selo.sekost.core.data.repository.PropertyRepository
import id.my.selo.sekost.core.data.repository.RoomRepository
import id.my.selo.sekost.core.data.repository.SettingsRepository
import id.my.selo.sekost.core.domain.ObserveRoomOverviewsUseCase
import id.my.selo.sekost.core.domain.PortfolioSummary
import id.my.selo.sekost.core.domain.RoomOverview
import id.my.selo.sekost.core.domain.TimeProvider
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.Room
import id.my.selo.sekost.core.model.RoomStatus
import id.my.selo.sekost.navigation.PropertyRoute
import id.my.selo.sekost.navigation.requireLong
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PropertyDetailUiState {
    data object Loading : PropertyDetailUiState
    data object NotFound : PropertyDetailUiState
    data class Ready(
        val property: Property,
        val globalFee: Long,
        val summary: PortfolioSummary,
        val filter: RoomStatus?,
        val rooms: List<RoomOverview>,
        val counts: Map<RoomStatus, Int>,
        /** Every room label in this property regardless of [filter], for numbering new rooms. */
        val roomLabels: List<String>,
    ) : PropertyDetailUiState
}

@HiltViewModel
class PropertyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeRoomOverviews: ObserveRoomOverviewsUseCase,
    private val properties: PropertyRepository,
    private val rooms: RoomRepository,
    settings: SettingsRepository,
    time: TimeProvider,
) : ViewModel() {

    val propertyId: Long = savedStateHandle.requireLong(PropertyRoute::propertyId.name)
    private val filter = MutableStateFlow<RoomStatus?>(null)

    val uiState: StateFlow<PropertyDetailUiState> = combine(
        properties.observe(propertyId),
        observeRoomOverviews(time.currentPeriod()),
        settings.settings,
        filter,
    ) { property, overviews, appSettings, statusFilter ->
        if (property == null) return@combine PropertyDetailUiState.NotFound
        val own = overviews.filter { it.room.propertyId == propertyId }
        PropertyDetailUiState.Ready(
            property = property,
            globalFee = appSettings.globalMonthlyFee,
            summary = PortfolioSummary.from(own),
            filter = statusFilter,
            rooms = if (statusFilter == null) own else own.filter { it.room.status == statusFilter },
            counts = RoomStatus.entries.associateWith { s -> own.count { it.room.status == s } },
            roomLabels = own.map { it.room.label },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PropertyDetailUiState.Loading)

    fun setFilter(status: RoomStatus?) {
        filter.value = status
    }

    fun saveProperty(property: Property) {
        viewModelScope.launch { properties.save(property.copy(id = propertyId)) }
    }

    fun saveRoom(room: Room) {
        viewModelScope.launch { rooms.save(room.copy(propertyId = propertyId)) }
    }

    fun addRooms(labels: List<String>, floor: String) {
        if (labels.isEmpty()) return
        viewModelScope.launch {
            rooms.saveAll(labels.map { Room(propertyId = propertyId, label = it, floor = floor) })
        }
    }

    fun deleteProperty(onDeleted: () -> Unit) {
        viewModelScope.launch {
            properties.delete(propertyId)
            onDeleted()
        }
    }
}
