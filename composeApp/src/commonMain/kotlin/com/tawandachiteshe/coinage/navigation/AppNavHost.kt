package com.tawandachiteshe.coinage.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tawandachiteshe.coinage.feature.add.AddScreen
import com.tawandachiteshe.coinage.feature.debt.DebtScreen
import com.tawandachiteshe.coinage.feature.goals.GoalsScreen
import com.tawandachiteshe.coinage.feature.home.HomeScreen
import com.tawandachiteshe.coinage.feature.insights.InsightsScreen
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
                onAddClick = { navController.navigate(Route.Add) },
            )
        }

        composable<Route.Goals> {
            GoalsScreen(
                onTabClick = { tab -> navController.navigateToTab(tab) },
                onAddClick = { navController.navigate(Route.Add) },
            )
        }

        composable<Route.Debt> {
            DebtScreen(
                onTabClick = { tab -> navController.navigateToTab(tab) },
                onAddClick = { navController.navigate(Route.Add) },
            )
        }

        composable<Route.Insights> {
            InsightsScreen(
                onTabClick = { tab -> navController.navigateToTab(tab) },
                onAddClick = { navController.navigate(Route.Add) },
            )
        }

        composable<Route.Add> {
            AddScreen(
                onTabClick = { tab -> navController.navigateToTab(tab) },
                onAddClick = {},
                onSaved = { navController.popBackStack() },
            )
        }

        composable<Route.Profile> {
            ProfileScreen(
                onTabClick = { tab -> navController.navigateToTab(tab) },
                onAddClick = { navController.navigate(Route.Add) },
                onOpenSettings = { navController.navigate(Route.Settings) },
            )
        }

        composable<Route.Settings> {
            SettingsScreen(
                onTabClick = { tab -> navController.navigateToTab(tab) },
                onAddClick = { navController.navigate(Route.Add) },
            )
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