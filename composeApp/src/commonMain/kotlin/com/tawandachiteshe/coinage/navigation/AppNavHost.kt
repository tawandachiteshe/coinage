package com.tawandachiteshe.coinage.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tawandachiteshe.coinage.data.UserPrefsRepository
import com.tawandachiteshe.coinage.feature.add.AddScreen
import com.tawandachiteshe.coinage.feature.add.AddType
import com.tawandachiteshe.coinage.feature.debt.DebtScreen
import com.tawandachiteshe.coinage.feature.goals.GoalsScreen
import com.tawandachiteshe.coinage.feature.home.HomeScreen
import com.tawandachiteshe.coinage.feature.insights.InsightsScreen
import com.tawandachiteshe.coinage.feature.jars.JarsManagerScreen
import com.tawandachiteshe.coinage.feature.onboarding.OnboardingScreen
import com.tawandachiteshe.coinage.feature.profile.ProfileScreen
import com.tawandachiteshe.coinage.feature.settings.SettingsScreen
import com.tawandachiteshe.coinage.ui.components.CoinageTab
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val prefs by koinInject<UserPrefsRepository>().getFlow().collectAsState(initial = null)
    // Render nothing until the first DB row arrives (single SQLite read, imperceptible).
    val prefs_ = prefs ?: return
    val startDestination: Route = if (prefs_.onboarding_done == 1L) Route.Home else Route.Onboarding

    var addType by remember { mutableStateOf<AddType?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = startDestination) {

            composable<Route.Onboarding> {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Route.Home) {
                            popUpTo(Route.Onboarding) { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.Home> {
                HomeScreen(
                    onTabClick = { tab -> navController.navigateToTab(tab) },
                    onAddClick = { addType = AddType.Transaction },
                    onManageJars = { navController.navigate(Route.JarsManager) },
                    onProfileClick = { navController.navigate(Route.Profile) },
                )
            }

            composable<Route.Goals> {
                GoalsScreen(
                    onTabClick = { tab -> navController.navigateToTab(tab) },
                    onAddClick = { addType = AddType.Goal },
                )
            }

            composable<Route.Debt> {
                DebtScreen(
                    onTabClick = { tab -> navController.navigateToTab(tab) },
                    onAddClick = { addType = AddType.Debt },
                )
            }

            composable<Route.Insights> {
                InsightsScreen(
                    onTabClick = { tab -> navController.navigateToTab(tab) },
                    onAddClick = { addType = AddType.Transaction },
                )
            }

            composable<Route.Profile> {
                ProfileScreen(
                    onTabClick = { tab -> navController.navigateToTab(tab) },
                    onAddClick = { addType = AddType.Transaction },
                    onOpenSettings = { navController.navigate(Route.Settings) },
                    onBack = { navController.popBackStack() },
                    onManageJars = { navController.navigate(Route.JarsManager) },
                )
            }

            composable<Route.Settings> {
                SettingsScreen(onBack = { navController.popBackStack() })
            }

            composable<Route.JarsManager> {
                JarsManagerScreen(onBack = { navController.popBackStack() })
            }
        }

        // In-place overlay — shown above the current screen without navigation
        if (addType != null) {
            AddScreen(
                initialType = addType!!,
                onDismiss = { addType = null },
            )
        }
    }
}

private fun NavHostController.navigateToTab(tab: CoinageTab) {
    val route: Route = when (tab) {
        CoinageTab.Home     -> Route.Home
        CoinageTab.Goals    -> Route.Goals
        CoinageTab.Debt     -> Route.Debt
        CoinageTab.Insights -> Route.Insights
    }
    navigate(route) {
        popUpTo(Route.Home) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}