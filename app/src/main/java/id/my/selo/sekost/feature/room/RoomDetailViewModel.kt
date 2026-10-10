package id.my.selo.sekost.feature.room

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.selo.sekost.core.data.repository.IssueRepository
import id.my.selo.sekost.core.data.repository.PaymentRepository
import id.my.selo.sekost.core.data.repository.PropertyRepository
import id.my.selo.sekost.core.data.repository.RoomRepository
import id.my.selo.sekost.core.data.repository.SettingsRepository
import id.my.selo.sekost.core.data.repository.TenantRepository
import id.my.selo.sekost.core.domain.FeeResolver
import id.my.selo.sekost.core.domain.ResolvedFee
import id.my.selo.sekost.core.domain.TimeProvider
import id.my.selo.sekost.core.model.Issue
import id.my.selo.sekost.core.model.IssueType
import id.my.selo.sekost.core.model.Payment
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.Room
import id.my.selo.sekost.core.model.RoomStatus
import id.my.selo.sekost.core.model.Tenant
import id.my.selo.sekost.navigation.RoomRoute
import id.my.selo.sekost.navigation.requireLong
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RoomDetailUiState {
    data object Loading : RoomDetailUiState
    data object NotFound : RoomDetailUiState
    data class Ready(
        val room: Room,
        val property: Property?,
        val tenant: Tenant?,
        val pastTenants: List<Tenant>,
        val fee: ResolvedFee,
        /** The fee this room would have without its own override — shown in the fee editor. */
        val inheritedFee: ResolvedFee,
        val period: YearMonth,
        val today: LocalDate,
        val dueDate: LocalDate,
        val currentPayment: Payment?,
        val paymentHistory: List<Payment>,
        val issues: List<Issue>,
    ) : RoomDetailUiState {
        val isLate: Boolean get() = tenant != null && currentPayment == null && today.isAfter(dueDate)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RoomDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val rooms: RoomRepository,
    properties: PropertyRepository,
    private val tenants: TenantRepository,
    private val payments: PaymentRepository,
    private val issues: IssueRepository,
    settings: SettingsRepository,
    private val time: TimeProvider,
) : ViewModel() {

    val roomId: Long = savedStateHandle.requireLong(RoomRoute::roomId.name)

    private val roomAndProperty = rooms.observe(roomId).flatMapLatest { room ->
        if (room == null) flowOf(null to null) else properties.observe(room.propertyId).let { p -> combine(flowOf(room), p, ::Pair) }
    }

    val uiState: StateFlow<RoomDetailUiState> = combine(
        roomAndProperty,
        tenants.observeHistory(roomId),
        payments.observeForRoom(roomId),
        issues.observeForRoom(roomId),
        settings.settings,
    ) { (room, property), tenantHistory, roomPayments, roomIssues, appSettings ->
        if (room == null) return@combine RoomDetailUiState.NotFound
        val today = time.today()
        val period = time.currentPeriod()
        RoomDetailUiState.Ready(
            room = room,
            property = property,
            tenant = tenantHistory.firstOrNull { it.isActive },
            pastTenants = tenantHistory.filterNot { it.isActive },
            fee = FeeResolver.resolve(room, property, appSettings.globalMonthlyFee),
            inheritedFee = FeeResolver.resolve(room.copy(customMonthlyFee = null), property, appSettings.globalMonthlyFee),
            period = period,
            today = today,
            dueDate = period.atDay(appSettings.dueDay.coerceAtMost(period.lengthOfMonth())),
            currentPayment = roomPayments.firstOrNull { it.period == period },
            paymentHistory = roomPayments,
            issues = roomIssues,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoomDetailUiState.Loading)

    private val ready get() = uiState.value as? RoomDetailUiState.Ready

    /** Only for transitions that don't involve a tenant; see [checkIn] / [checkOut]. */
    fun setStatus(status: RoomStatus) = launch { rooms.setStatus(roomId, status) }

    fun checkIn(name: String, phone: String, date: LocalDate) = launch { tenants.checkIn(roomId, name, phone, date) }

    fun updateTenant(tenant: Tenant) = launch { tenants.update(tenant) }

    /** Ends the tenancy; the room becomes [then] (vacant by default, or maintenance). */
    fun checkOut(then: RoomStatus = RoomStatus.VACANT) = launch {
        tenants.checkOut(roomId, time.today())
        if (then != RoomStatus.VACANT) rooms.setStatus(roomId, then)
    }

    fun setCustomFee(fee: Long?) = launch { rooms.setCustomFee(roomId, fee) }

    fun markPaid() = launch {
        val state = ready ?: return@launch
        payments.markPaid(roomId, state.tenant?.id, state.period, state.fee.amount, time.today())
    }

    fun markUnpaid() = launch { payments.markUnpaid(roomId, time.currentPeriod()) }

    fun reportIssue(type: IssueType, title: String, description: String) = launch {
        issues.save(Issue(roomId = roomId, type = type, title = title, description = description, reportedOn = time.today()))
    }

    fun resolveIssue(id: Long) = launch { issues.resolve(id, time.today()) }

    fun reopenIssue(id: Long) = launch { issues.reopen(id) }

    fun deleteIssue(id: Long) = launch { issues.delete(id) }

    fun saveRoom(room: Room) = launch { rooms.save(room) }

    fun deleteRoom(onDeleted: () -> Unit) = launch {
        rooms.delete(roomId)
        onDeleted()
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
