package com.tawandachiteshe.expensify.di

import com.tawandachiteshe.expensify.data.CategoryRepository
import com.tawandachiteshe.expensify.data.DebtRepository
import com.tawandachiteshe.expensify.data.GoalRepository
import com.tawandachiteshe.expensify.data.TransactionRepository
import com.tawandachiteshe.expensify.data.createHttpClient
import org.koin.dsl.module

val appModule = module {
    single { createHttpClient(get()) }
    single { CategoryRepository(get()) }
    single { TransactionRepository(get()) }
    single { DebtRepository(get()) }
    single { GoalRepository(get()) }
}