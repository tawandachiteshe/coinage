package com.tawandachiteshe.coinage.feature.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.tawandachiteshe.coinage.ui.theme.CoinageColors

fun FragmentActivity.promptBiometric(onSuccess: () -> Unit, onCancel: () -> Unit) {
    val manager = BiometricManager.from(this)
    if (manager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_SUCCESS) {
        // No biometrics enrolled or no hardware — skip gate
        onSuccess()
        return
    }

    BiometricPrompt(
        this,
        ContextCompat.getMainExecutor(this),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onCancel()
        },
    ).authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Coinage")
            .setSubtitle("Use fingerprint or face to continue")
            .setNegativeButtonText("Cancel")
            .build()
    )
}

@Composable
fun BiometricLockScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoinageColors.Ink),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = CoinageColors.Tangerine,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Coinage",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                color = CoinageColors.Paper,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Authenticate to continue",
                fontSize = 14.sp,
                color = CoinageColors.Paper.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}