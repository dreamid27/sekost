package id.my.selo.sekost.feature.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import id.my.selo.sekost.core.data.repository.SampleDataSeeder
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.PropertyType
import id.my.selo.sekost.testing.FakeDataGraph
import id.my.selo.sekost.testing.ImmediateTransactionRunner
import id.my.selo.sekost.testing.MainDispatcherRule
import java.time.LocalTime
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val graph = FakeDataGraph()
    private val seeder =
        SampleDataSeeder(graph.properties, graph.rooms, graph.tenants, graph.payments, graph.issues, graph.time, ImmediateTransactionRunner)
    private fun viewModel() = HomeViewModel(graph.observeRoomOverviews, graph.properties, graph.settings, seeder, graph.time)

    @Test fun `starts empty and shows a property after saving`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            val empty = expectMostRecentItem() as HomeUiState.Ready
            assertThat(empty.properties).isEmpty()

            vm.saveProperty(Property(name = "Kost Kenanga", type = PropertyType.KOST))
            val after = expectMostRecentItem() as HomeUiState.Ready
            assertThat(after.properties.map { it.property.name }).containsExactly("Kost Kenanga")
        }
    }

    @Test fun `sample data produces a realistic portfolio`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            expectMostRecentItem()
            vm.loadSampleData()
            val state = expectMostRecentItem() as HomeUiState.Ready
            assertThat(state.properties).hasSize(2)
            assertThat(state.summary.totalRooms).isEqualTo(9)
            assertThat(state.summary.occupied).isEqualTo(6)
            assertThat(state.summary.maintenance).isEqualTo(1)
            assertThat(state.summary.paidCount).isEqualTo(3)
            assertThat(state.summary.openIssues).isEqualTo(3)

            vm.loadSampleData() // second tap must not duplicate
            expectNoEvents()
            assertThat((vm.uiState.value as HomeUiState.Ready).properties).hasSize(2)
        }
    }

    @Test fun `greeting follows time of day`() {
        assertThat(HomeViewModel.dayPartOf(LocalTime.of(7, 0))).isEqualTo(DayPart.MORNING)
        assertThat(HomeViewModel.dayPartOf(LocalTime.of(12, 0))).isEqualTo(DayPart.AFTERNOON)
        assertThat(HomeViewModel.dayPartOf(LocalTime.of(16, 0))).isEqualTo(DayPart.EVENING)
        assertThat(HomeViewModel.dayPartOf(LocalTime.of(22, 0))).isEqualTo(DayPart.NIGHT)
    }
}
