package com.tawandachiteshe.coinage.di

import androidx.datastore.preferences.preferencesDataStore
import com.tawandachiteshe.coinage.data.DatastoreTokenStorage
import com.tawandachiteshe.coinage.data.DatabaseDriverFactory
import com.tawandachiteshe.coinage.data.GoogleAuthRepositoryImpl
import com.tawandachiteshe.coinage.data.TokenStorage
import com.tawandachiteshe.coinage.db.ExpensifyDatabase
import com.tawandachiteshe.coinage.domain.repository.GoogleAuthRepository
import com.tawandachiteshe.coinage.R
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
            clientId = androidContext().getString(R.string.google_client_id),
        )
    }
    single { DatabaseDriverFactory(androidContext()) }
    single { ExpensifyDatabase(get<DatabaseDriverFactory>().create()) }
}

private val android.content.Context.tokenDataStore by preferencesDataStore(name = "coinage_tokens")
private val android.content.Context.googleDataStore by preferencesDataStore(name = "coinage_google")
