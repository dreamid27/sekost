package id.my.selo.sekost.core.domain

import com.google.common.truth.Truth.assertThat
import id.my.selo.sekost.core.model.Issue
import id.my.selo.sekost.core.model.IssueStatus
import id.my.selo.sekost.core.model.IssueType
import id.my.selo.sekost.core.model.Payment
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.PropertyType
import id.my.selo.sekost.core.model.Room
import id.my.selo.sekost.core.model.RoomStatus
import id.my.selo.sekost.core.model.Tenant
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Test

class PortfolioSummaryTest {
    private val day = LocalDate.of(2026, 10, 5)
    private val period = YearMonth.of(2026, 10)
    private val property = Property(1, "Kost", PropertyType.KOST, defaultMonthlyFee = 1_000_000)
    private val rooms = listOf(
        Room(1, 1, "A", status = RoomStatus.OCCUPIED, customMonthlyFee = 1_500_000),
        Room(2, 1, "B", status = RoomStatus.OCCUPIED),
        Room(3, 1, "C", status = RoomStatus.VACANT),
        Room(4, 1, "D", status = RoomStatus.MAINTENANCE),
    )
    private val tenants = listOf(Tenant(1, 1, "Rina", checkInDate = day), Tenant(2, 2, "Bagus", checkInDate = day))
    private val payments = listOf(Payment(1, 1, 1, period, 1_500_000, day))
    private val issues = listOf(
        Issue(1, 4, IssueType.MAINTENANCE, "Kran", reportedOn = day),
        Issue(2, 2, IssueType.COMPLAINT, "Wi-Fi", reportedOn = day),
        Issue(3, 2, IssueType.COMPLAINT, "Old", status = IssueStatus.RESOLVED, reportedOn = day),
    )

    private val overviews = buildRoomOverviews(rooms, listOf(property), tenants, payments, issues, globalFee = 900_000)

    @Test fun `overviews join tenant, fee, payment and open issues`() {
        val a = overviews.first { it.room.id == 1L }
        assertThat(a.tenant?.name).isEqualTo("Rina")
        assertThat(a.fee).isEqualTo(ResolvedFee(1_500_000, FeeSource.ROOM))
        assertThat(a.isPaid).isTrue()

        val b = overviews.first { it.room.id == 2L }
        assertThat(b.fee.source).isEqualTo(FeeSource.PROPERTY)
        assertThat(b.needsPayment).isTrue()
        assertThat(b.openComplaints).isEqualTo(1) // resolved complaint is ignored
    }

    @Test fun `summary counts statuses and money`() {
        val summary = PortfolioSummary.from(overviews)
        assertThat(summary.totalRooms).isEqualTo(4)
        assertThat(summary.occupied).isEqualTo(2)
        assertThat(summary.vacant).isEqualTo(1)
        assertThat(summary.maintenance).isEqualTo(1)
        assertThat(summary.expectedIncome).isEqualTo(2_500_000)
        assertThat(summary.collectedIncome).isEqualTo(1_500_000)
        assertThat(summary.outstandingIncome).isEqualTo(1_000_000)
        assertThat(summary.paidCount).isEqualTo(1)
        assertThat(summary.unpaidCount).isEqualTo(1)
        assertThat(summary.openIssues).isEqualTo(2)
        assertThat(summary.occupancyRate).isEqualTo(0.5f)
    }

    @Test fun `empty portfolio has zero rates instead of NaN`() {
        assertThat(PortfolioSummary.from(emptyList()).occupancyRate).isEqualTo(0f)
        assertThat(PortfolioSummary.from(emptyList()).collectionRate).isEqualTo(0f)
    }
}
