package id.my.selo.sekost.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.my.selo.sekost.R
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.feature.billing.BillingRoute
import id.my.selo.sekost.feature.home.HomeRoute
import id.my.selo.sekost.feature.issues.IssuesRoute
import id.my.selo.sekost.feature.onboarding.OnboardingRoute
import id.my.selo.sekost.feature.property.PropertyDetailRoute
import id.my.selo.sekost.feature.room.RoomDetailRoute
import id.my.selo.sekost.feature.settings.SettingsRoute
import id.my.selo.sekost.navigation.BillingRoute
import id.my.selo.sekost.navigation.HomeRoute
import id.my.selo.sekost.navigation.IssuesRoute
import id.my.selo.sekost.navigation.OnboardingRoute
import id.my.selo.sekost.navigation.PropertyRoute
import id.my.selo.sekost.navigation.RoomRoute
import id.my.selo.sekost.navigation.SettingsRoute
import kotlin.reflect.KClass

enum class TopLevelDestination(val route: Any, val routeClass: KClass<*>, val icon: ImageVector, val labelRes: Int, val testTag: String) {
    Home(HomeRoute, HomeRoute::class, SekostIcons.Home, R.string.nav_home, "nav_home"),
    Billing(BillingRoute, BillingRoute::class, SekostIcons.Bill, R.string.nav_billing, "nav_billing"),
    Issues(IssuesRoute, IssuesRoute::class, SekostIcons.Clipboard, R.string.nav_issues, "nav_issues"),
    Settings(SettingsRoute, SettingsRoute::class, SekostIcons.Sliders, R.string.nav_settings, "nav_settings"),
}

@Composable
fun SekostApp(startWithOnboarding: Boolean = false, navController: NavHostController = rememberNavController()) {
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val currentTopLevel = TopLevelDestination.entries.firstOrNull { destination.isOn(it.routeClass) }

    Scaffold(
        containerColor = SekostTheme.colors.background,
        // Screens handle their own status-bar insets (headers scroll with content).
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            AnimatedVisibility(
                visible = currentTopLevel != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                SekostNavigationBar(
                    selected = currentTopLevel,
                    onSelect = { navController.navigateToTopLevel(it) },
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            SekostNavHost(navController, startWithOnboarding)
            // Translucent scrim so scrolled content never collides with status bar icons.
            Box(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(SekostTheme.colors.background.copy(alpha = 0.94f)),
            )
        }
    }
}

@Composable
private fun SekostNavHost(navController: NavHostController, startWithOnboarding: Boolean) {
    val duration = SekostTheme.motion.slow
    val openRoom: (Long) -> Unit = { navController.navigate(RoomRoute(it)) }
    NavHost(
        navController = navController,
        startDestination = if (startWithOnboarding) OnboardingRoute else HomeRoute,
        enterTransition = { fadeIn(tween(duration)) + slideInHorizontally(tween(duration)) { it / 6 } },
        exitTransition = { fadeOut(tween(duration / 2)) },
        popEnterTransition = { fadeIn(tween(duration)) },
        popExitTransition = { fadeOut(tween(duration / 2)) + slideOutHorizontally(tween(duration)) { it / 6 } },
    ) {
        composable<OnboardingRoute>(
            exitTransition = { fadeOut(tween(duration)) + scaleOut(tween(duration), targetScale = ONBOARDING_EXIT_SCALE) },
        ) {
            OnboardingRoute(
                onFinished = {
                    navController.navigate(HomeRoute) {
                        popUpTo<OnboardingRoute> { inclusive = true }
                    }
                },
            )
        }
        topLevel<HomeRoute> {
            HomeRoute(
                onOpenProperty = { navController.navigate(PropertyRoute(it)) },
                onOpenBilling = { navController.navigateToTopLevel(TopLevelDestination.Billing) },
                onOpenIssues = { navController.navigateToTopLevel(TopLevelDestination.Issues) },
            )
        }
        topLevel<BillingRoute> { BillingRoute(onOpenRoom = openRoom) }
        topLevel<IssuesRoute> { IssuesRoute(onOpenRoom = openRoom) }
        topLevel<SettingsRoute> { SettingsRoute() }
        composable<PropertyRoute> {
            PropertyDetailRoute(onBack = navController::popBackStack, onOpenRoom = openRoom)
        }
        composable<RoomRoute> {
            RoomDetailRoute(onBack = navController::popBackStack)
        }
    }
}

private const val TAB_FADE_MS = 200
private const val ONBOARDING_EXIT_SCALE = 1.04f

/** Tabs cross-fade instead of sliding, so switching feels like a view change, not navigation. */
private inline fun <reified T : Any> androidx.navigation.NavGraphBuilder.topLevel(
    noinline content: @Composable () -> Unit,
) = composable<T>(
    enterTransition = { if (initialState.destination.isTopLevel()) fadeIn(tween(TAB_FADE_MS)) else null },
    exitTransition = { if (targetState.destination.isTopLevel()) fadeOut(tween(TAB_FADE_MS)) else null },
    popEnterTransition = { if (initialState.destination.isTopLevel()) fadeIn(tween(TAB_FADE_MS)) else null },
) { content() }

private fun NavDestination.isTopLevel() = TopLevelDestination.entries.any { isOn(it.routeClass) }

private fun NavDestination?.isOn(route: KClass<*>): Boolean = this?.hierarchy?.any { it.hasRoute(route) } == true

private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
