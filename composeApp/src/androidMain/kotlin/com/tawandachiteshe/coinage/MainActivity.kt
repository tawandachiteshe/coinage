package com.tawandachiteshe.coinage

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.tawandachiteshe.coinage.data.UserPrefsRepository
import com.tawandachiteshe.coinage.di.androidModule
import com.tawandachiteshe.coinage.di.appModule
import com.tawandachiteshe.coinage.feature.auth.BiometricLockScreen
import com.tawandachiteshe.coinage.feature.auth.promptBiometric
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
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

class MainActivity : FragmentActivity() {

    private val prefsRepo: UserPrefsRepository by inject()
    private val _googleAuthResult = MutableSharedFlow<Intent?>(extraBufferCapacity = 1)
    val googleAuthResult = _googleAuthResult.asSharedFlow()

    private var authenticated by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            if (authenticated) App() else BiometricLockScreen()
        }
        if (savedInstanceState != null) {
            // Config change (rotation) — already authenticated this session
            authenticated = true
        } else {
            lifecycleScope.launch {
                val prefs = prefsRepo.get()
                if (prefs?.biometric_enabled == 1L) {
                    promptBiometric(
                        onSuccess = { authenticated = true },
                        onCancel = { finish() },
                    )
                } else {
                    authenticated = true
                }
            }
        }
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