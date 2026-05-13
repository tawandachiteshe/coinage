package com.tawandachiteshe.coinage.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Onboarding : Route
    @Serializable data object Home : Route
    @Serializable data object Goals : Route
    @Serializable data object Debt : Route
    @Serializable data object Insights : Route
    @Serializable data object Add : Route
    @Serializable data object Profile : Route
    @Serializable data object Settings : Route
}