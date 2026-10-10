package id.my.selo.sekost.core.data.repository

import androidx.room.withTransaction
import id.my.selo.sekost.core.data.database.SekostDatabase
import id.my.selo.sekost.core.data.database.dao.IssueDao
import id.my.selo.sekost.core.data.database.dao.PaymentDao
import id.my.selo.sekost.core.data.database.dao.PropertyDao
import id.my.selo.sekost.core.data.database.dao.RoomDao
import id.my.selo.sekost.core.data.database.dao.TenantDao
import id.my.selo.sekost.core.data.database.entity.PaymentEntity
import id.my.selo.sekost.core.data.database.entity.TenantEntity
import id.my.selo.sekost.core.data.database.entity.asEntity
import id.my.selo.sekost.core.data.database.entity.asModel
import id.my.selo.sekost.core.model.Issue
import id.my.selo.sekost.core.model.IssueStatus
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.Room
import id.my.selo.sekost.core.model.RoomStatus
import id.my.selo.sekost.core.model.Tenant
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OfflinePropertyRepository @Inject constructor(
    private val dao: PropertyDao,
) : PropertyRepository {
    override fun observeAll() = dao.observeAll().map { list -> list.map { it.asModel() } }
    override fun observe(id: Long) = dao.observe(id).map { it?.asModel() }
    override suspend fun count() = dao.count()
    override suspend fun save(property: Property) = dao.upsert(property.asEntity()).let { if (it == -1L) property.id else it }
    override suspend fun delete(id: Long) = dao.delete(id)
}

internal class OfflineRoomRepository @Inject constructor(
    private val dao: RoomDao,
) : RoomRepository {
    override fun observeAll() = dao.observeAll().map { list -> list.map { it.asModel() } }
    override fun observe(id: Long) = dao.observe(id).map { it?.asModel() }
    override suspend fun save(room: Room) = dao.upsert(room.asEntity()).let { if (it == -1L) room.id else it }
    override suspend fun setStatus(id: Long, status: RoomStatus) = dao.updateStatus(id, status)
    override suspend fun setCustomFee(id: Long, fee: Long?) = dao.updateCustomFee(id, fee)
    override suspend fun delete(id: Long) = dao.delete(id)
}

internal class OfflineTenantRepository @Inject constructor(
    private val db: SekostDatabase,
    private val dao: TenantDao,
    private val roomDao: RoomDao,
) : TenantRepository {
    override fun observeActive() = dao.observeActive().map { list -> list.map { it.asModel() } }
    override fun observeHistory(roomId: Long) = dao.observeForRoom(roomId).map { list -> list.map { it.asModel() } }

    override suspend fun checkIn(roomId: Long, name: String, phone: String, date: LocalDate): Long = db.withTransaction {
        dao.checkOutRoom(roomId, date)
        val id = dao.upsert(
            TenantEntity(roomId = roomId, name = name.trim(), phone = phone.trim(), checkInDate = date, checkOutDate = null),
        )
        roomDao.updateStatus(roomId, RoomStatus.OCCUPIED)
        id
    }

    override suspend fun update(tenant: Tenant) {
        dao.upsert(tenant.asEntity())
    }

    override suspend fun checkOut(roomId: Long, date: LocalDate) = db.withTransaction {
        dao.checkOutRoom(roomId, date)
        roomDao.updateStatus(roomId, RoomStatus.VACANT)
    }
}

internal class OfflinePaymentRepository @Inject constructor(
    private val dao: PaymentDao,
) : PaymentRepository {
    override fun observeForPeriod(period: YearMonth) = dao.observeForPeriod(period).map { list -> list.map { it.asModel() } }
    override fun observeForRoom(roomId: Long) = dao.observeForRoom(roomId).map { list -> list.map { it.asModel() } }

    override suspend fun markPaid(roomId: Long, tenantId: Long?, period: YearMonth, amount: Long, paidOn: LocalDate) {
        val existing = dao.find(roomId, period)
        dao.upsert(PaymentEntity(existing?.id ?: 0, roomId, tenantId, period, amount, paidOn))
    }

    override suspend fun markUnpaid(roomId: Long, period: YearMonth) = dao.delete(roomId, period)
}

internal class OfflineIssueRepository @Inject constructor(
    private val dao: IssueDao,
) : IssueRepository {
    override fun observeAll(): Flow<List<Issue>> = dao.observeAll().map { list -> list.map { it.asModel() } }
    override fun observeOpen(): Flow<List<Issue>> = dao.observeOpen().map { list -> list.map { it.asModel() } }
    override fun observeForRoom(roomId: Long) = dao.observeForRoom(roomId).map { list -> list.map { it.asModel() } }
    override suspend fun save(issue: Issue) = dao.upsert(issue.asEntity()).let { if (it == -1L) issue.id else it }
    override suspend fun resolve(id: Long, date: LocalDate) = dao.updateStatus(id, IssueStatus.RESOLVED, date)
    override suspend fun reopen(id: Long) = dao.updateStatus(id, IssueStatus.OPEN, null)
    override suspend fun delete(id: Long) = dao.delete(id)
}
