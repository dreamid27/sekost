package id.my.selo.sekost.core.data.repository

import id.my.selo.sekost.core.model.AppSettings
import id.my.selo.sekost.core.model.Issue
import id.my.selo.sekost.core.model.Payment
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.Room
import id.my.selo.sekost.core.model.RoomStatus
import id.my.selo.sekost.core.model.Tenant
import id.my.selo.sekost.core.model.ThemeMode
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

/*
 * Repository contracts. ViewModels depend on these interfaces only, which keeps them
 * testable with the in-memory fakes in `src/test/.../testing`.
 */

interface PropertyRepository {
    fun observeAll(): Flow<List<Property>>
    fun observe(id: Long): Flow<Property?>
    suspend fun count(): Int
    suspend fun save(property: Property): Long
    suspend fun delete(id: Long)
}

interface RoomRepository {
    fun observeAll(): Flow<List<Room>>
    fun observe(id: Long): Flow<Room?>
    suspend fun save(room: Room): Long

    /** Creates several new rooms atomically. */
    suspend fun saveAll(rooms: List<Room>): List<Long>
    suspend fun setStatus(id: Long, status: RoomStatus)
    suspend fun setCustomFee(id: Long, fee: Long?)
    suspend fun delete(id: Long)
}

interface TenantRepository {
    fun observeActive(): Flow<List<Tenant>>
    fun observeHistory(roomId: Long): Flow<List<Tenant>>

    /** Moves a tenant into a room and marks the room occupied, atomically. */
    suspend fun checkIn(roomId: Long, name: String, phone: String, date: LocalDate): Long
    suspend fun update(tenant: Tenant)

    /** Ends the active tenancy of a room and marks the room vacant, atomically. */
    suspend fun checkOut(roomId: Long, date: LocalDate)
}

interface PaymentRepository {
    fun observeForPeriod(period: YearMonth): Flow<List<Payment>>
    fun observeForRoom(roomId: Long): Flow<List<Payment>>
    suspend fun markPaid(roomId: Long, tenantId: Long?, period: YearMonth, amount: Long, paidOn: LocalDate)
    suspend fun markUnpaid(roomId: Long, period: YearMonth)
}

interface IssueRepository {
    fun observeAll(): Flow<List<Issue>>
    fun observeOpen(): Flow<List<Issue>>
    fun observeForRoom(roomId: Long): Flow<List<Issue>>
    suspend fun save(issue: Issue): Long
    suspend fun resolve(id: Long, date: LocalDate)
    suspend fun reopen(id: Long)
    suspend fun delete(id: Long)
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setOwnerName(name: String)
    suspend fun setGlobalMonthlyFee(fee: Long)
    suspend fun setDueDay(day: Int)
    suspend fun setThemeMode(mode: ThemeMode)

    /** Saves the first-run setup and marks onboarding as done, in one atomic write. */
    suspend fun completeOnboarding(ownerName: String, globalMonthlyFee: Long, dueDay: Int)
}
