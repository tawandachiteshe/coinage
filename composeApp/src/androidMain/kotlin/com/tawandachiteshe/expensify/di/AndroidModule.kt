package com.tawandachiteshe.expensify.di

import androidx.datastore.preferences.preferencesDataStore
import com.tawandachiteshe.expensify.data.DatastoreTokenStorage
import com.tawandachiteshe.expensify.data.DatabaseDriverFactory
import com.tawandachiteshe.expensify.data.GoogleAuthRepositoryImpl
import com.tawandachiteshe.expensify.data.TokenStorage
import com.tawandachiteshe.expensify.db.ExpensifyDatabase
import com.tawandachiteshe.expensify.domain.repository.GoogleAuthRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single<TokenStorage> {
        DatastoreTokenStorage(androidContext().tokenDataStore)
    }
    single<GoogleAuthRepository> {
        GoogleAuthRepositoryImpl(
            context = androidContext(),
            dataStore = androidContext().googleDataStore,
        )
    }
    single { DatabaseDriverFactory(androidContext()) }
    single { ExpensifyDatabase(get<DatabaseDriverFactory>().create()) }
}

private val android.content.Context.tokenDataStore by preferencesDataStore(name = "expensify_tokens")
private val android.content.Context.googleDataStore by preferencesDataStore(name = "expensify_google")
