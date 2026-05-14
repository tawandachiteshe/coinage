package com.tawandachiteshe.coinage

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tawandachiteshe.coinage.di.androidModule
import com.tawandachiteshe.coinage.di.appModule
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ExpensifyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ExpensifyApp)
            modules(androidModule, appModule)
        }
    }
}

const val GOOGLE_AUTH_REQUEST_CODE = 9001

class MainActivity : ComponentActivity() {

    private val _googleAuthResult = MutableSharedFlow<Intent?>(extraBufferCapacity = 1)
    val googleAuthResult = _googleAuthResult.asSharedFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }

    @Deprecated("Required for GIS Authorization API direct launch")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_AUTH_REQUEST_CODE) {
            _googleAuthResult.tryEmit(data)
        }
    }
}