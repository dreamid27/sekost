package id.my.selo.sekost.core.domain

import id.my.selo.sekost.core.data.repository.IssueRepository
import id.my.selo.sekost.core.data.repository.PaymentRepository
import id.my.selo.sekost.core.data.repository.PropertyRepository
import id.my.selo.sekost.core.data.repository.RoomRepository
import id.my.selo.sekost.core.data.repository.SettingsRepository
import id.my.selo.sekost.core.data.repository.TenantRepository
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Live list of every room joined with its property, tenant, fee, payment and open issues. */
class ObserveRoomOverviewsUseCase @Inject constructor(
    private val properties: PropertyRepository,
    private val rooms: RoomRepository,
    private val tenants: TenantRepository,
    private val payments: PaymentRepository,
    private val issues: IssueRepository,
    private val settings: SettingsRepository,
) {
    operator fun invoke(period: YearMonth): Flow<List<RoomOverview>> = combine(
        rooms.observeAll(),
        properties.observeAll(),
        tenants.observeActive(),
        payments.observeForPeriod(period),
        combine(issues.observeOpen(), settings.settings.map { it.globalMonthlyFee }.distinctUntilChanged(), ::Pair),
    ) { rooms, properties, tenants, payments, (openIssues, globalFee) ->
        buildRoomOverviews(rooms, properties, tenants, payments, openIssues, globalFee)
    }
}
