package id.my.selo.sekost.feature.billing

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import id.my.selo.sekost.core.model.AppSettings
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.PropertyType
import id.my.selo.sekost.core.model.Room
import id.my.selo.sekost.testing.FakeDataGraph
import id.my.selo.sekost.testing.MainDispatcherRule
import java.time.YearMonth
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class BillingViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val graph = FakeDataGraph(AppSettings(globalMonthlyFee = 800_000))

    private fun viewModel() = BillingViewModel(graph.observeRoomOverviews, graph.payments, graph.settings, graph.time)

    @Test fun `only occupied rooms are billed and filters split paid and unpaid`() = runTest {
        val property = graph.properties.save(Property(name = "Rumah", type = PropertyType.HOUSE))
        val r1 = graph.rooms.save(Room(propertyId = property, label = "Kamar 1"))
        val r2 = graph.rooms.save(Room(propertyId = property, label = "Kamar 2"))
        graph.rooms.save(Room(propertyId = property, label = "Kamar 3")) // vacant
        graph.tenants.checkIn(r1, "Rina", "", graph.time.today())
        graph.tenants.checkIn(r2, "Dimas", "", graph.time.today())

        val vm = viewModel()
        vm.uiState.test {
            val start = expectMostRecentItem()
            assertThat(start.billable).hasSize(2)
            assertThat(start.visible.map { it.room.label }).containsExactly("Kamar 1", "Kamar 2")
            assertThat(start.summary.expectedIncome).isEqualTo(1_600_000)

            vm.markPaid(start.visible.first { it.room.id == r1 })
            val afterPay = expectMostRecentItem()
            assertThat(afterPay.visible.map { it.room.id }).containsExactly(r2)
            assertThat(afterPay.summary.collectedIncome).isEqualTo(800_000)

            vm.setFilter(BillingFilter.PAID)
            assertThat(expectMostRecentItem().visible.map { it.room.id }).containsExactly(r1)
        }
    }

    @Test fun `month navigation stops one month ahead and tenants are not billed before move-in`() = runTest {
        val property = graph.properties.save(Property(name = "Kost", type = PropertyType.KOST))
        val room = graph.rooms.save(Room(propertyId = property, label = "A"))
        graph.tenants.checkIn(room, "Ayu", "", graph.time.today())

        val vm = viewModel()
        vm.uiState.test {
            expectMostRecentItem()
            vm.previousMonth()
            val previous = expectMostRecentItem()
            assertThat(previous.period).isEqualTo(YearMonth.of(2026, 9))
            assertThat(previous.billable).isEmpty()

            vm.nextMonth()
            vm.nextMonth()
            val next = expectMostRecentItem()
            assertThat(next.period).isEqualTo(YearMonth.of(2026, 11))
            assertThat(next.canGoNext).isFalse()
            vm.nextMonth()
            assertThat(vm.uiState.value.period).isEqualTo(YearMonth.of(2026, 11))
        }
    }
}
