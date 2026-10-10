package id.my.selo.sekost.feature.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.selo.sekost.core.data.repository.PaymentRepository
import id.my.selo.sekost.core.data.repository.SettingsRepository
import id.my.selo.sekost.core.domain.ObserveRoomOverviewsUseCase
import id.my.selo.sekost.core.domain.PortfolioSummary
import id.my.selo.sekost.core.domain.RoomOverview
import id.my.selo.sekost.core.domain.TimeProvider
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BillingFilter { UNPAID, PAID, ALL }

data class BillingUiState(
    val period: YearMonth,
    val isCurrentPeriod: Boolean,
    val canGoNext: Boolean,
    val filter: BillingFilter = BillingFilter.UNPAID,
    val summary: PortfolioSummary = PortfolioSummary.Empty,
    /** Rooms that are billable this period: occupied now, or already paid. */
    val billable: List<RoomOverview> = emptyList(),
    val visible: List<RoomOverview> = emptyList(),
    val dueDay: Int = 10,
    val today: LocalDate,
) {
    fun isLate(overview: RoomOverview): Boolean =
        overview.needsPayment && today.isAfter(period.atDay(dueDay.coerceAtMost(period.lengthOfMonth())))
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BillingViewModel @Inject constructor(
    private val observeRoomOverviews: ObserveRoomOverviewsUseCase,
    private val payments: PaymentRepository,
    settings: SettingsRepository,
    private val time: TimeProvider,
) : ViewModel() {

    private val currentPeriod = time.currentPeriod()
    private val period = MutableStateFlow(currentPeriod)
    private val filter = MutableStateFlow(BillingFilter.UNPAID)

    val uiState: StateFlow<BillingUiState> = combine(
        period.flatMapLatest { p -> observeRoomOverviews(p).map { it to p } },
        filter,
        settings.settings,
    ) { (overviews, p), f, appSettings ->
        // A room is billable for a period if it was paid, or is occupied by someone who had moved in by then.
        val billable = overviews.filter { o ->
            o.isPaid || (o.isOccupied && o.tenant?.checkInDate?.let { !YearMonth.from(it).isAfter(p) } != false)
        }
        BillingUiState(
            period = p,
            isCurrentPeriod = p == currentPeriod,
            canGoNext = p < currentPeriod.plusMonths(1),
            filter = f,
            summary = PortfolioSummary.from(billable),
            billable = billable,
            visible = when (f) {
                BillingFilter.UNPAID -> billable.filter { it.needsPayment }
                BillingFilter.PAID -> billable.filter { it.isPaid }
                BillingFilter.ALL -> billable
            },
            dueDay = appSettings.dueDay,
            today = time.today(),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        BillingUiState(period = currentPeriod, isCurrentPeriod = true, canGoNext = true, today = time.today()),
    )

    fun previousMonth() {
        period.value = period.value.minusMonths(1)
    }

    fun nextMonth() {
        if (uiState.value.canGoNext) period.value = period.value.plusMonths(1)
    }

    fun setFilter(value: BillingFilter) {
        filter.value = value
    }

    fun markPaid(overview: RoomOverview) {
        viewModelScope.launch {
            payments.markPaid(overview.room.id, overview.tenant?.id, period.value, overview.fee.amount, time.today())
        }
    }

    fun markUnpaid(overview: RoomOverview) {
        viewModelScope.launch { payments.markUnpaid(overview.room.id, period.value) }
    }
}
