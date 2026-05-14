package com.tawandachiteshe.coinage.feature.settings

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import com.tawandachiteshe.coinage.GOOGLE_AUTH_REQUEST_CODE
import com.tawandachiteshe.coinage.MainActivity
import com.tawandachiteshe.coinage.data.GoogleAuthRepositoryImpl
import com.tawandachiteshe.coinage.domain.repository.GoogleAuthRepository
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.CoinageDialog
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.CoinageColors
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val log = Logger.withTag("GoogleConnect")

@Composable
actual fun GoogleConnectSection(
    repository: GoogleAuthRepository,
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
) {
    val impl = repository as GoogleAuthRepositoryImpl
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as? MainActivity

    val isConnected = state.isGoogleConnected
    val email = state.googleEmail

    // Collect the result forwarded by MainActivity.onActivityResult
    LaunchedEffect(activity) {
        activity?.googleAuthResult?.collect { data ->
            log.d { "onActivityResult received: data=${data?.extras?.keySet()?.joinToString()}" }
            impl.handleAuthorizationResult(data)
            val connected = impl.isConnected()
            log.i { "after handleAuthorizationResult: isConnected=$connected" }
            onAction(SettingsAction.RefreshGoogleState)
        }
    }

    // Restore confirm dialog
    if (state.showRestoreConfirm) {
        CoinageDialog(
            title = "Replace all data?",
            onDismissRequest = { onAction(SettingsAction.DismissRestoreConfirm) },
            confirmLabel = "Replace",
            confirmColor = CoinageColors.Cherry,
            onConfirm = { onAction(SettingsAction.OnConfirmRestore) },
            dismissLabel = "Cancel",
            onDismiss = { onAction(SettingsAction.DismissRestoreConfirm) },
        ) {
            Text(
                "This will delete everything on this device and replace it with your Drive backup. This cannot be undone.",
                fontSize = 13.sp,
                color = CoinageColors.Ink2,
            )
        }
    }

    StickerCard(
        bgColor = CoinageColors.PaperWhite,
        modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
        cornerRadius = 16.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape)
                            .background(CoinageColors.Butter).border(1.4.dp, CoinageColors.Ink, CircleShape)
                    )
                    Text("G", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CoinageColors.Ink)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Google Drive", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CoinageColors.Ink)
                    Text(
                        if (isConnected) "backup & spreadsheet sync" else "back up your data to Drive",
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.3.sp,
                        color = CoinageColors.Ink2.copy(alpha = 0.6f),
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isConnected) CoinageColors.Mint.copy(alpha = 0.25f) else CoinageColors.Paper2)
                        .border(
                            1.2.dp,
                            if (isConnected) CoinageColors.Mint else CoinageColors.Ink.copy(alpha = 0.3f),
                            RoundedCornerShape(999.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        if (isConnected) "connected" else "not connected",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = if (isConnected) CoinageColors.Mint else CoinageColors.Ink2.copy(alpha = 0.5f),
                    )
                }
            }

            if (isConnected && email != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    email,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontStyle = FontStyle.Italic,
                    color = CoinageColors.Ink2.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 42.dp),
                )
            }

            // Last backup label
            state.lastBackupInfo?.let { info ->
                Spacer(Modifier.height(4.dp))
                Text(
                    "Last backup: ${info.modifiedAt.toBackupLabel()} · ${info.sizeBytes.toSizeLabel()}",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CoinageColors.Ink2.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 42.dp),
                )
            }

            // Error label
            state.backupError?.let { err ->
                Spacer(Modifier.height(6.dp))
                Text(
                    err,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CoinageColors.Cherry,
                    modifier = Modifier.padding(start = 42.dp)
                        .clickable { onAction(SettingsAction.DismissBackupError) },
                )
            }

            Spacer(Modifier.height(14.dp))

            if (state.isSyncing) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = CoinageColors.Grape,
                        strokeWidth = 2.5.dp,
                    )
                }
            } else if (isConnected) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DriveActionChip("Backup", CoinageColors.Grape, modifier = Modifier.weight(1f)) {
                        onAction(SettingsAction.OnBackupNow)
                    }
                    DriveActionChip("Restore", CoinageColors.Sky, modifier = Modifier.weight(1f)) {
                        onAction(SettingsAction.OnRestoreFromDrive)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DriveActionChip("Sheets", CoinageColors.Mint, modifier = Modifier.weight(1f)) {
                        onAction(SettingsAction.OnSyncToSheets)
                    }
                    DriveActionChip("Disconnect", CoinageColors.Paper2) {
                        scope.launch {
                            impl.signOut()
                            onAction(SettingsAction.RefreshGoogleState)
                        }
                    }
                }
            } else {
                DriveActionChip(
                    label = "Connect Google account",
                    color = CoinageColors.Butter,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    scope.launch {
                        log.d { "Connect tapped — calling requestAuthorization" }
                        val result = impl.requestAuthorization()
                        when {
                            result.hasResolution() -> {
                                log.d { "hasResolution=true → launching consent UI via startIntentSenderForResult" }
                                @Suppress("DEPRECATION")
                                activity?.startIntentSenderForResult(
                                    result.pendingIntent!!.intentSender,
                                    GOOGLE_AUTH_REQUEST_CODE,
                                    null, 0, 0, 0
                                )
                            }
                            result.accessToken != null -> {
                                log.i { "accessToken already available (no UI needed) → saving token" }
                                impl.saveToken(result.accessToken!!, null)
                                onAction(SettingsAction.RefreshGoogleState)
                            }
                            else -> log.w { "requestAuthorization returned no resolution and no token — unexpected state" }
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
            .border(1.4.dp, CoinageColors.Ink, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CoinageColors.Ink)
    }
}

private fun Long.toBackupLabel(): String {
    val dt = Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val month = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")[dt.monthNumber - 1]
    return "$month ${dt.dayOfMonth}"
}

private fun Long.toSizeLabel(): String = when {
    this >= 1_048_576 -> "${this / 1_048_576} MB"
    this >= 1_024     -> "${this / 1_024} KB"
    else              -> "$this B"
}