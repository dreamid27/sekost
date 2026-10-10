package id.my.selo.sekost.testing

import id.my.selo.sekost.core.data.repository.IssueRepository
import id.my.selo.sekost.core.data.repository.PaymentRepository
import id.my.selo.sekost.core.data.repository.PropertyRepository
import id.my.selo.sekost.core.data.repository.RoomRepository
import id.my.selo.sekost.core.data.repository.SettingsRepository
import id.my.selo.sekost.core.data.repository.TenantRepository
import id.my.selo.sekost.core.domain.TimeProvider
import id.my.selo.sekost.core.model.AppSettings
import id.my.selo.sekost.core.model.Issue
import id.my.selo.sekost.core.model.IssueStatus
import id.my.selo.sekost.core.model.Payment
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.Room
import id.my.selo.sekost.core.model.RoomStatus
import id.my.selo.sekost.core.model.Tenant
import id.my.selo.sekost.core.model.ThemeMode
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/*
 * In-memory fakes that honor the same contracts as the Room-backed repositories,
 * including the side effects of check-in/check-out on room status.
 */

class FixedTimeProvider(var date: LocalDate = LocalDate.of(2026, 10, 15)) : TimeProvider {
    override fun today(): LocalDate = date
}

class FakePropertyRepository : PropertyRepository {
    val items = MutableStateFlow<List<Property>>(emptyList())
    private var nextId = 1L
    override fun observeAll() = items.map { list -> list.sortedBy { it.name } }
    override fun observe(id: Long) = items.map { list -> list.firstOrNull { it.id == id } }
    override suspend fun count() = items.value.size
    override suspend fun save(property: Property): Long {
        val id = if (property.id == 0L) nextId++ else property.id
        items.update { list -> list.filterNot { it.id == id } + property.copy(id = id) }
        return id
    }
    override suspend fun delete(id: Long) = items.update { list -> list.filterNot { it.id == id } }
}

class FakeRoomRepository : RoomRepository {
    val items = MutableStateFlow<List<Room>>(emptyList())
    private var nextId = 1L
    override fun observeAll(): Flow<List<Room>> = items
    override fun observe(id: Long) = items.map { list -> list.firstOrNull { it.id == id } }
    override suspend fun save(room: Room): Long {
        val id = if (room.id == 0L) nextId++ else room.id
        items.update { list -> list.filterNot { it.id == id } + room.copy(id = id) }
        return id
    }
    override suspend fun saveAll(rooms: List<Room>): List<Long> {
        val created = rooms.map { it.copy(id = nextId++) }
        items.update { it + created }
        return created.map { it.id }
    }
    override suspend fun setStatus(id: Long, status: RoomStatus) = modify(id) { it.copy(status = status) }
    override suspend fun setCustomFee(id: Long, fee: Long?) = modify(id) { it.copy(customMonthlyFee = fee) }
    override suspend fun delete(id: Long) = items.update { list -> list.filterNot { it.id == id } }
    private fun modify(id: Long, block: (Room) -> Room) = items.update { list -> list.map { if (it.id == id) block(it) else it } }
}

class FakeTenantRepository(private val rooms: FakeRoomRepository) : TenantRepository {
    val items = MutableStateFlow<List<Tenant>>(emptyList())
    private var nextId = 1L
    override fun observeActive() = items.map { list -> list.filter { it.isActive } }
    override fun observeHistory(roomId: Long) =
        items.map { list -> list.filter { it.roomId == roomId }.sortedByDescending { it.checkInDate } }
    override suspend fun checkIn(roomId: Long, name: String, phone: String, date: LocalDate): Long {
        endTenancy(roomId, date)
        val id = nextId++
        items.update { it + Tenant(id, roomId, name, phone, date) }
        rooms.setStatus(roomId, RoomStatus.OCCUPIED)
        return id
    }
    override suspend fun update(tenant: Tenant) = items.update { list -> list.map { if (it.id == tenant.id) tenant else it } }
    override suspend fun checkOut(roomId: Long, date: LocalDate) {
        endTenancy(roomId, date)
        rooms.setStatus(roomId, RoomStatus.VACANT)
    }
    private fun endTenancy(roomId: Long, date: LocalDate) =
        items.update { list -> list.map { if (it.roomId == roomId && it.isActive) it.copy(checkOutDate = date) else it } }
}

class FakePaymentRepository : PaymentRepository {
    val items = MutableStateFlow<List<Payment>>(emptyList())
    private var nextId = 1L
    override fun observeForPeriod(period: YearMonth) = items.map { list -> list.filter { it.period == period } }
    override fun observeForRoom(roomId: Long) = items.map { list -> list.filter { it.roomId == roomId }.sortedByDescending { it.period } }
    override suspend fun markPaid(roomId: Long, tenantId: Long?, period: YearMonth, amount: Long, paidOn: LocalDate) {
        markUnpaid(roomId, period)
        items.update { it + Payment(nextId++, roomId, tenantId, period, amount, paidOn) }
    }
    override suspend fun markUnpaid(roomId: Long, period: YearMonth) =
        items.update { list -> list.filterNot { it.roomId == roomId && it.period == period } }
}

class FakeIssueRepository : IssueRepository {
    val items = MutableStateFlow<List<Issue>>(emptyList())
    private var nextId = 1L
    override fun observeAll(): Flow<List<Issue>> = items
    override fun observeOpen() = items.map { list -> list.filter { it.status == IssueStatus.OPEN } }
    override fun observeForRoom(roomId: Long) = items.map { list -> list.filter { it.roomId == roomId } }
    override suspend fun save(issue: Issue): Long {
        val id = if (issue.id == 0L) nextId++ else issue.id
        items.update { list -> list.filterNot { it.id == id } + issue.copy(id = id) }
        return id
    }
    override suspend fun resolve(id: Long, date: LocalDate) = modify(id) { it.copy(status = IssueStatus.RESOLVED, resolvedOn = date) }
    override suspend fun reopen(id: Long) = modify(id) { it.copy(status = IssueStatus.OPEN, resolvedOn = null) }
    override suspend fun delete(id: Long) = items.update { list -> list.filterNot { it.id == id } }
    private fun modify(id: Long, block: (Issue) -> Issue) = items.update { list -> list.map { if (it.id == id) block(it) else it } }
}

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    val state = MutableStateFlow(initial)
    override val settings: Flow<AppSettings> = state
    override suspend fun setOwnerName(name: String) = state.update { it.copy(ownerName = name) }
    override suspend fun setGlobalMonthlyFee(fee: Long) = state.update { it.copy(globalMonthlyFee = fee) }
    override suspend fun setDueDay(day: Int) = state.update { it.copy(dueDay = day) }
    override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
    override suspend fun completeOnboarding(ownerName: String, globalMonthlyFee: Long, dueDay: Int) = state.update {
        it.copy(
            ownerName = ownerName.trim(),
            globalMonthlyFee = globalMonthlyFee,
            dueDay = dueDay.coerceIn(1, 28),
            onboardingCompleted = true
        )
    }
}

object ImmediateTransactionRunner : id.my.selo.sekost.core.data.repository.TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T = block()
}

/** One object wiring all fakes together, mirroring the Hilt graph. */
class FakeDataGraph(settings: AppSettings = AppSettings()) {
    val time = FixedTimeProvider()
    val properties = FakePropertyRepository()
    val rooms = FakeRoomRepository()
    val tenants = FakeTenantRepository(rooms)
    val payments = FakePaymentRepository()
    val issues = FakeIssueRepository()
    val settings = FakeSettingsRepository(settings)
    val observeRoomOverviews = id.my.selo.sekost.core.domain.ObserveRoomOverviewsUseCase(
        properties,
        rooms,
        tenants,
        payments,
        issues,
        this.settings,
    )
}
