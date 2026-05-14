package com.tawandachiteshe.coinage.feature.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.tawandachiteshe.coinage.domain.repository.GoogleAuthRepository
import com.tawandachiteshe.coinage.ui.theme.CoinageColors

@Composable
actual fun GoogleConnectSection(
    repository: GoogleAuthRepository,
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
) {
    Text(
        "Google Drive sync — coming to iOS soon.",
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = CoinageColors.Ink2.copy(alpha = 0.5f),
    )
}