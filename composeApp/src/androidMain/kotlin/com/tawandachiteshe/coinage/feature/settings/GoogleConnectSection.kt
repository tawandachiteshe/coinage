package com.tawandachiteshe.coinage.feature.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontStyle
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
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row — Drive logo placeholder + title
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Google Drive colour dot cluster
                Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape)
                        .background(TrackerColors.Butter).border(1.4.dp, TrackerColors.Ink, CircleShape))
                    Text("G", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TrackerColors.Ink)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Google Drive",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrackerColors.Ink,
                    )
                    Text(
                        if (isConnected) "backup & spreadsheet sync" else "back up your data to Drive",
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.3.sp,
                        color = TrackerColors.Ink2.copy(alpha = 0.6f),
                    )
                }
                Spacer(Modifier.weight(1f))
                // Connection status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isConnected) TrackerColors.Mint.copy(alpha = 0.25f) else TrackerColors.Paper2)
                        .border(1.2.dp, if (isConnected) TrackerColors.Mint else TrackerColors.Ink.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        if (isConnected) "connected" else "not connected",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = if (isConnected) TrackerColors.Mint else TrackerColors.Ink2.copy(alpha = 0.5f),
                    )
                }
            }

            if (isConnected && email != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    email!!,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontStyle = FontStyle.Italic,
                    color = TrackerColors.Ink2.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 42.dp),
                )
            }

            Spacer(Modifier.height(14.dp))

            if (isConnected) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DriveActionChip("Sync now", TrackerColors.Grape, modifier = Modifier.weight(1f)) {
                        // SheetsSyncRepository.sync() — wired when ready
                    }
                    DriveActionChip("Disconnect", TrackerColors.Paper2) {
                        scope.launch {
                            impl.signOut()
                            isConnected = false
                        }
                    }
                }
            } else {
                DriveActionChip(
                    label = "Connect Google account",
                    color = TrackerColors.Butter,
                    modifier = Modifier.fillMaxWidth(),
                ) {
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
private fun DriveActionChip(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .popShadow(cornerRadius = 10.dp, offsetX = 2.dp, offsetY = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color, RoundedCornerShape(10.dp))
            .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
    }
}