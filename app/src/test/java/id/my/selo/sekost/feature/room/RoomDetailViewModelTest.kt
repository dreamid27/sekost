package id.my.selo.sekost.feature.room

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import id.my.selo.sekost.core.domain.FeeSource
import id.my.selo.sekost.core.model.IssueStatus
import id.my.selo.sekost.core.model.IssueType
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.PropertyType
import id.my.selo.sekost.core.model.Room
import id.my.selo.sekost.core.model.RoomStatus
import id.my.selo.sekost.testing.FakeDataGraph
import id.my.selo.sekost.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RoomDetailViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val graph = FakeDataGraph()
    private lateinit var viewModel: RoomDetailViewModel
    private var roomId = 0L

    @Before fun setUp() = runTest {
        val propertyId = graph.properties.save(Property(name = "Kost Melati", type = PropertyType.KOST, defaultMonthlyFee = 1_200_000))
        roomId = graph.rooms.save(Room(propertyId = propertyId, label = "Kamar 01"))
        viewModel = RoomDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("roomId" to roomId)),
            rooms = graph.rooms,
            properties = graph.properties,
            tenants = graph.tenants,
            payments = graph.payments,
            issues = graph.issues,
            settings = graph.settings,
            time = graph.time,
        )
    }

    private suspend fun ReceiveTurbine<RoomDetailUiState>.awaitReady(): RoomDetailUiState.Ready {
        while (true) {
            (awaitItem() as? RoomDetailUiState.Ready)?.let { return it }
        }
    }

    @Test fun `fee follows property, then custom override, then back`() = runTest {
        viewModel.uiState.test {
            val initial = awaitReady()
            assertThat(initial.fee.amount).isEqualTo(1_200_000)
            assertThat(initial.fee.source).isEqualTo(FeeSource.PROPERTY)

            viewModel.setCustomFee(1_750_000)
            val custom = awaitItem() as RoomDetailUiState.Ready
            assertThat(custom.fee.amount).isEqualTo(1_750_000)
            assertThat(custom.fee.source).isEqualTo(FeeSource.ROOM)
            assertThat(custom.inheritedFee.amount).isEqualTo(1_200_000)

            viewModel.setCustomFee(null)
            assertThat((awaitItem() as RoomDetailUiState.Ready).fee.source).isEqualTo(FeeSource.PROPERTY)
        }
    }

    @Test fun `check in occupies room, payment toggles, checkout to maintenance`() = runTest {
        viewModel.uiState.test {
            awaitReady()
            viewModel.checkIn("Rina Kartika", "0812", graph.time.today())
            val occupied = expectMostRecentItem() as RoomDetailUiState.Ready
            assertThat(occupied.room.status).isEqualTo(RoomStatus.OCCUPIED)
            assertThat(occupied.tenant?.name).isEqualTo("Rina Kartika")
            assertThat(occupied.currentPayment).isNull()

            viewModel.markPaid()
            val paid = expectMostRecentItem() as RoomDetailUiState.Ready
            assertThat(paid.currentPayment?.amount).isEqualTo(1_200_000)

            viewModel.markUnpaid()
            assertThat((expectMostRecentItem() as RoomDetailUiState.Ready).currentPayment).isNull()

            viewModel.checkOut(then = RoomStatus.MAINTENANCE)
            val after = expectMostRecentItem() as RoomDetailUiState.Ready
            assertThat(after.tenant).isNull()
            assertThat(after.pastTenants.map { it.name }).containsExactly("Rina Kartika")
            assertThat(after.room.status).isEqualTo(RoomStatus.MAINTENANCE)
        }
    }

    @Test fun `issues can be reported, resolved and reopened`() = runTest {
        viewModel.uiState.test {
            awaitReady()
            viewModel.reportIssue(IssueType.COMPLAINT, "Wi-Fi putus", "")
            val issue = (expectMostRecentItem() as RoomDetailUiState.Ready).issues.single()
            assertThat(issue.status).isEqualTo(IssueStatus.OPEN)

            viewModel.resolveIssue(issue.id)
            val resolved = (expectMostRecentItem() as RoomDetailUiState.Ready).issues.single()
            assertThat(resolved.status).isEqualTo(IssueStatus.RESOLVED)
            assertThat(resolved.resolvedOn).isEqualTo(graph.time.today())

            viewModel.reopenIssue(issue.id)
            assertThat((expectMostRecentItem() as RoomDetailUiState.Ready).issues.single().status).isEqualTo(IssueStatus.OPEN)
        }
    }

    @Test fun `unpaid after due day is late`() = runTest {
        viewModel.uiState.test {
            awaitReady()
            graph.settings.setDueDay(10) // fixed "today" is the 15th
            viewModel.checkIn("Bagus", "", graph.time.today().minusMonths(1))
            assertThat((expectMostRecentItem() as RoomDetailUiState.Ready).isLate).isTrue()
            graph.settings.setDueDay(20)
            assertThat((expectMostRecentItem() as RoomDetailUiState.Ready).isLate).isFalse()
        }
    }
}
