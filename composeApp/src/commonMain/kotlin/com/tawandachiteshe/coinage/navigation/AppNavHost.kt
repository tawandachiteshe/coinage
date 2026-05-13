package com.tawandachiteshe.coinage.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
import com.tawandachiteshe.coinage.ui.components.TrackerTab

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: Route = Route.Onboarding,
) {
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
                onAddClick = { navController.navigate(Route.Add("Transaction")) },
                onManageJars = { navController.navigate(Route.JarsManager) },
            )
        }

        composable<Route.Goals> {
            GoalsScreen(
                onTabClick = { tab -> navController.navigateToTab(tab) },
                onAddClick = { navController.navigate(Route.Add("Goal")) },
            )
        }

        composable<Route.Debt> {
            DebtScreen(
                onTabClick = { tab -> navController.navigateToTab(tab) },
                onAddClick = { navController.navigate(Route.Add("Debt")) },
            )
        }

        composable<Route.Insights> {
            InsightsScreen(
                onTabClick = { tab -> navController.navigateToTab(tab) },
                onAddClick = { navController.navigate(Route.Add("Transaction")) },
            )
        }

        composable<Route.Add> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Add>()
            val initialType = AddType.entries.firstOrNull { it.name == route.type } ?: AddType.Transaction
            AddScreen(
                initialType = initialType,
                onDismiss = { navController.popBackStack() },
            )
        }

        composable<Route.Profile> {
            ProfileScreen(
                onTabClick = { tab -> navController.navigateToTab(tab) },
                onAddClick = { navController.navigate(Route.Add("Transaction")) },
                onOpenSettings = { navController.navigate(Route.Settings) },
            )
        }

        composable<Route.Settings> {
            SettingsScreen(
                onTabClick = { tab -> navController.navigateToTab(tab) },
                onAddClick = { navController.navigate(Route.Add("Transaction")) },
            )
        }

        composable<Route.JarsManager> {
            JarsManagerScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun NavHostController.navigateToTab(tab: TrackerTab) {
    val route: Route = when (tab) {
        TrackerTab.Home     -> Route.Home
        TrackerTab.Goals    -> Route.Goals
        TrackerTab.Debt     -> Route.Debt
        TrackerTab.Insights -> Route.Insights
    }
    navigate(route) {
        popUpTo(Route.Home) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}