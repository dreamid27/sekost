package id.my.selo.sekost.feature.onboarding

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import id.my.selo.sekost.MainViewModel
import id.my.selo.sekost.core.data.repository.SampleDataSeeder
import id.my.selo.sekost.core.model.AppSettings
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.PropertyType
import id.my.selo.sekost.core.model.ThemeMode
import id.my.selo.sekost.testing.FakeDataGraph
import id.my.selo.sekost.testing.ImmediateTransactionRunner
import id.my.selo.sekost.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private fun seederFor(graph: FakeDataGraph) =
        SampleDataSeeder(graph.properties, graph.rooms, graph.tenants, graph.payments, graph.issues, graph.time, ImmediateTransactionRunner)

    private fun viewModel(graph: FakeDataGraph) = OnboardingViewModel(graph.settings, seederFor(graph))

    @Test fun `walks through every step in order and back`() = runTest {
        val vm = viewModel(FakeDataGraph())
        vm.uiState.test {
            assertThat(expectMostRecentItem().step).isEqualTo(OnboardingStep.Welcome)
            repeat(4) { vm.next() }
            assertThat(expectMostRecentItem().step).isEqualTo(OnboardingStep.Ready)
            vm.next() // stays on the last step
            assertThat(vm.uiState.value.step).isEqualTo(OnboardingStep.Ready)

            assertThat(vm.back()).isTrue()
            assertThat(expectMostRecentItem().step).isEqualTo(OnboardingStep.Appearance)
            vm.goTo(OnboardingStep.Welcome)
            assertThat(expectMostRecentItem().step).isEqualTo(OnboardingStep.Welcome)
            assertThat(vm.back()).isFalse()
        }
    }

    @Test fun `billing step blocks continuing without a fee`() = runTest {
        val vm = viewModel(FakeDataGraph())
        vm.uiState.test {
            vm.goTo(OnboardingStep.Billing)
            vm.setFeeDigits("")
            val blocked = expectMostRecentItem()
            assertThat(blocked.canContinue).isFalse()
            vm.next()
            assertThat(vm.uiState.value.step).isEqualTo(OnboardingStep.Billing)

            vm.setFeeDigits("850000")
            vm.next()
            assertThat(expectMostRecentItem().step).isEqualTo(OnboardingStep.Appearance)
        }
    }

    @Test fun `prefills from saved settings`() = runTest {
        val graph = FakeDataGraph(AppSettings(ownerName = "Rina", globalMonthlyFee = 1_250_000, dueDay = 5))
        val vm = viewModel(graph)
        vm.uiState.test {
            val state = expectMostRecentItem()
            assertThat(state.ownerName).isEqualTo("Rina")
            assertThat(state.globalFee).isEqualTo(1_250_000)
            assertThat(state.dueDay).isEqualTo(5)
        }
    }

    @Test fun `theme applies immediately`() = runTest {
        val graph = FakeDataGraph()
        val vm = viewModel(graph)
        vm.uiState.test {
            vm.setTheme(ThemeMode.DARK)
            assertThat(expectMostRecentItem().themeMode).isEqualTo(ThemeMode.DARK)
            assertThat(graph.settings.state.value.themeMode).isEqualTo(ThemeMode.DARK)
            assertThat(graph.settings.state.value.onboardingCompleted).isFalse()
        }
    }

    @Test fun `finishing saves the setup and marks onboarding complete`() = runTest {
        val graph = FakeDataGraph()
        val vm = viewModel(graph)
        vm.uiState.test {
            vm.setOwnerName("  Bu Sari ")
            vm.setFeeDigits("1500000")
            vm.setDueDay(40) // clamped to the last allowed day
            vm.finish()
            assertThat(expectMostRecentItem().isFinished).isTrue()
        }
        val saved = graph.settings.state.value
        assertThat(saved.ownerName).isEqualTo("Bu Sari")
        assertThat(saved.globalMonthlyFee).isEqualTo(1_500_000)
        assertThat(saved.dueDay).isEqualTo(28)
        assertThat(saved.onboardingCompleted).isTrue()
        assertThat(graph.properties.observeAll().first()).isEmpty()
    }

    @Test fun `skipping with an invalid fee falls back to the default`() = runTest {
        val graph = FakeDataGraph()
        val vm = viewModel(graph)
        vm.uiState.test {
            vm.setFeeDigits("")
            vm.finish()
            assertThat(expectMostRecentItem().isFinished).isTrue()
        }
        assertThat(graph.settings.state.value.globalMonthlyFee).isEqualTo(AppSettings.DEFAULT_GLOBAL_FEE)
        assertThat(graph.settings.state.value.onboardingCompleted).isTrue()
    }

    @Test fun `finishing with sample data seeds the demo portfolio`() = runTest {
        val graph = FakeDataGraph()
        val vm = viewModel(graph)
        vm.uiState.test {
            vm.finish(withSampleData = true)
            assertThat(expectMostRecentItem().isFinished).isTrue()
        }
        assertThat(graph.properties.observeAll().first()).hasSize(2)
        assertThat(graph.settings.state.value.onboardingCompleted).isTrue()
    }

    @Test fun `only fresh installs start in onboarding`() = runTest {
        val fresh = FakeDataGraph()
        assertThat(MainViewModel(fresh.settings, fresh.properties).startWithOnboarding.first { it != null }).isTrue()

        val done = FakeDataGraph(AppSettings(onboardingCompleted = true))
        assertThat(MainViewModel(done.settings, done.properties).startWithOnboarding.first { it != null }).isFalse()

        // People who used Sekost before onboarding existed already have properties: don't interrupt them.
        val existing = FakeDataGraph()
        existing.properties.save(Property(name = "Kost Melati", type = PropertyType.KOST))
        assertThat(MainViewModel(existing.settings, existing.properties).startWithOnboarding.first { it != null }).isFalse()
    }
}
