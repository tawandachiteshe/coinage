package com.tawandachiteshe.coinage.feature.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tawandachiteshe.coinage.data.GoogleAuthRepositoryImpl
import com.tawandachiteshe.coinage.domain.repository.GoogleAuthRepository
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import kotlinx.coroutines.launch

@Composable
actual fun GoogleConnectSection(repository: GoogleAuthRepository) {
    val impl = repository as GoogleAuthRepositoryImpl
    val scope = rememberCoroutineScope()

    var isConnected by remember { mutableStateOf(impl.isConnected()) }
    val email by produceState<String?>(null, isConnected) {
        value = if (isConnected) impl.getConnectedEmail() else null
    }

    val launcher = rememberLauncherForActivityResult(StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            scope.launch {
                impl.handleAuthorizationResult(result.data)
                isConnected = impl.isConnected()
            }
        }
    }

    StickerCard(
        bgColor = TrackerColors.PaperWhite,
        modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
        cornerRadius = 16.dp,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Google Sheets".uppercase(),
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.4.sp,
                color = TrackerColors.Ink2.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (isConnected) "Sync your finances to a Google Sheet you own."
                else "Connect to export your data to Google Sheets.",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = TrackerColors.Ink2.copy(alpha = 0.6f),
                letterSpacing = 0.3.sp,
            )
            Spacer(Modifier.height(12.dp))

            if (isConnected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Connected", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Mint)
                        if (email != null) Text(email!!, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.6f))
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionChip("Sync now", TrackerColors.Grape) {
                            // SheetsSyncRepository.sync() — wired when SQLDelight is ready
                        }
                        ActionChip("Disconnect", TrackerColors.Coral) {
                            scope.launch {
                                impl.signOut()
                                isConnected = false
                            }
                        }
                    }
                }
            } else {
                ActionChip("Connect Google Account", TrackerColors.Butter) {
                    scope.launch {
                        val result = impl.requestAuthorization()
                        when {
                            result.hasResolution() -> launcher.launch(
                                IntentSenderRequest.Builder(result.pendingIntent!!.intentSender).build()
                            )
                            result.accessToken != null -> {
                                impl.saveToken(result.accessToken!!, null)
                                isConnected = true
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionChip(label: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .popShadow(cornerRadius = 999.dp, offsetX = 2.dp, offsetY = 2.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(color, RoundedCornerShape(999.dp))
            .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
    }
}