package com.tawandachiteshe.coinage.di

import com.tawandachiteshe.coinage.data.CategoryRepository
import com.tawandachiteshe.coinage.data.CurrencyRepository
import com.tawandachiteshe.coinage.data.DebtRepository
import com.tawandachiteshe.coinage.data.GoalRepository
import com.tawandachiteshe.coinage.data.IouRepository
import com.tawandachiteshe.coinage.data.TransactionRepository
import com.tawandachiteshe.coinage.data.UserPrefsRepository
import com.tawandachiteshe.coinage.data.UserProfileRepository
import com.tawandachiteshe.coinage.data.createHttpClient
import com.tawandachiteshe.coinage.feature.add.AddViewModel
import com.tawandachiteshe.coinage.feature.debt.DebtViewModel
import com.tawandachiteshe.coinage.feature.debt.IouViewModel
import com.tawandachiteshe.coinage.feature.goals.GoalsViewModel
import com.tawandachiteshe.coinage.feature.home.HomeViewModel
import com.tawandachiteshe.coinage.feature.insights.InsightsViewModel
import com.tawandachiteshe.coinage.feature.jars.JarsManagerViewModel
import com.tawandachiteshe.coinage.feature.onboarding.OnboardingViewModel
import com.tawandachiteshe.coinage.feature.profile.ProfileViewModel
import com.tawandachiteshe.coinage.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { createHttpClient(get()) }
    single { CategoryRepository(get()) }
    single { CurrencyRepository(get()) }
    single { TransactionRepository(get()) }
    single { DebtRepository(get()) }
    single { GoalRepository(get()) }
    single { IouRepository(get()) }
    single { UserProfileRepository(get()) }
    single { UserPrefsRepository(get()) }

    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { GoalsViewModel(get(), get(), get()) }
    viewModel { DebtViewModel(get(), get(), get()) }
    viewModel { IouViewModel(get(), get(), get(), get()) }
    viewModel { InsightsViewModel(get(), get()) }
    viewModel { AddViewModel(get(), get(), get(), get(), get()) }
    viewModel { JarsManagerViewModel(get(), get()) }
    viewModel { OnboardingViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(currencyRepo = get(), userPrefsRepo = get(), txRepo = get(), goalRepo = get(), debtRepo = get(), googleAuthRepo = getOrNull(), backupOrchestrator = getOrNull()) }
}