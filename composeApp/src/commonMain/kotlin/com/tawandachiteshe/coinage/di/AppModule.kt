package com.tawandachiteshe.coinage.di

import com.tawandachiteshe.coinage.data.CategoryRepository
import com.tawandachiteshe.coinage.data.DebtRepository
import com.tawandachiteshe.coinage.data.GoalRepository
import com.tawandachiteshe.coinage.data.TransactionRepository
import com.tawandachiteshe.coinage.data.createHttpClient
import org.koin.dsl.module

val appModule = module {
    single { createHttpClient(get()) }
    single { CategoryRepository(get()) }
    single { TransactionRepository(get()) }
    single { DebtRepository(get()) }
    single { GoalRepository(get()) }
}