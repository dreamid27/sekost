package id.my.selo.sekost.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import id.my.selo.sekost.core.data.database.SekostDatabase
import id.my.selo.sekost.core.data.repository.OfflineIssueRepository
import id.my.selo.sekost.core.data.repository.OfflinePaymentRepository
import id.my.selo.sekost.core.data.repository.OfflinePropertyRepository
import id.my.selo.sekost.core.data.repository.OfflineRoomRepository
import id.my.selo.sekost.core.data.repository.OfflineTenantRepository
import id.my.selo.sekost.core.model.Issue
import id.my.selo.sekost.core.model.IssueStatus
import id.my.selo.sekost.core.model.IssueType
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.PropertyType
import id.my.selo.sekost.core.model.Room as RoomModel
import id.my.selo.sekost.core.model.RoomStatus
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineRepositoriesTest {
    private lateinit var db: SekostDatabase
    private lateinit var properties: OfflinePropertyRepository
    private lateinit var rooms: OfflineRoomRepository
    private lateinit var tenants: OfflineTenantRepository
    private lateinit var payments: OfflinePaymentRepository
    private lateinit var issues: OfflineIssueRepository
    private val today = LocalDate.of(2026, 10, 10)
    private val period = YearMonth.of(2026, 10)

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SekostDatabase::class.java).build()
        properties = OfflinePropertyRepository(db.propertyDao())
        rooms = OfflineRoomRepository(db.roomDao())
        tenants = OfflineTenantRepository(db, db.tenantDao(), db.roomDao())
        payments = OfflinePaymentRepository(db.paymentDao())
        issues = OfflineIssueRepository(db.issueDao())
    }

    @After fun tearDown() = db.close()

    private suspend fun newRoom(): Long {
        val propertyId = properties.save(Property(name = "Kost Melati", type = PropertyType.KOST))
        return rooms.save(RoomModel(propertyId = propertyId, label = "Kamar 01"))
    }

    @Test fun saveReturnsIdForInsertAndUpdate() = runTest {
        val id = properties.save(Property(name = "A", type = PropertyType.KOST))
        val sameId = properties.save(Property(id = id, name = "B", type = PropertyType.HOUSE, defaultMonthlyFee = 500_000))
        assertThat(sameId).isEqualTo(id)
        assertThat(properties.observe(id).first()?.defaultMonthlyFee).isEqualTo(500_000)
    }

    @Test fun checkInAndCheckOutUpdateRoomStatusAtomically() = runTest {
        val roomId = newRoom()
        tenants.checkIn(roomId, "Rina", "0812", today)
        assertThat(rooms.observe(roomId).first()?.status).isEqualTo(RoomStatus.OCCUPIED)
        assertThat(tenants.observeActive().first().single().name).isEqualTo("Rina")

        // A second check-in replaces the active tenant instead of leaving two active.
        tenants.checkIn(roomId, "Bagus", "", today.plusDays(1))
        assertThat(tenants.observeActive().first().map { it.name }).containsExactly("Bagus")

        tenants.checkOut(roomId, today.plusDays(2))
        assertThat(rooms.observe(roomId).first()?.status).isEqualTo(RoomStatus.VACANT)
        assertThat(tenants.observeActive().first()).isEmpty()
        assertThat(tenants.observeHistory(roomId).first()).hasSize(2)
    }

    @Test fun markPaidTwiceKeepsOnePaymentPerPeriod() = runTest {
        val roomId = newRoom()
        payments.markPaid(roomId, null, period, 1_000_000, today)
        payments.markPaid(roomId, null, period, 1_200_000, today)
        val list = payments.observeForPeriod(period).first()
        assertThat(list).hasSize(1)
        assertThat(list.single().amount).isEqualTo(1_200_000)

        payments.markUnpaid(roomId, period)
        assertThat(payments.observeForPeriod(period).first()).isEmpty()
    }

    @Test fun deletingPropertyCascadesToEverything() = runTest {
        val roomId = newRoom()
        val propertyId = rooms.observe(roomId).first()!!.propertyId
        tenants.checkIn(roomId, "Rina", "", today)
        payments.markPaid(roomId, null, period, 1, today)
        issues.save(Issue(roomId = roomId, type = IssueType.MAINTENANCE, title = "Kran", reportedOn = today))

        properties.delete(propertyId)
        assertThat(rooms.observeAll().first()).isEmpty()
        assertThat(tenants.observeActive().first()).isEmpty()
        assertThat(payments.observeForPeriod(period).first()).isEmpty()
        assertThat(issues.observeAll().first()).isEmpty()
    }

    @Test fun issuesResolveAndReopen() = runTest {
        val roomId = newRoom()
        val id = issues.save(Issue(roomId = roomId, type = IssueType.COMPLAINT, title = "Wi-Fi", reportedOn = today))
        issues.resolve(id, today)
        assertThat(issues.observeOpen().first()).isEmpty()
        assertThat(issues.observeAll().first().single().status).isEqualTo(IssueStatus.RESOLVED)
        issues.reopen(id)
        assertThat(issues.observeOpen().first().single().resolvedOn).isNull()
    }
}
