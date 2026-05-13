package com.tawandachiteshe.coinage.feature.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.tawandachiteshe.coinage.domain.repository.GoogleAuthRepository
import com.tawandachiteshe.coinage.ui.theme.TrackerColors

@Composable
actual fun GoogleConnectSection(repository: GoogleAuthRepository) {
    Text(
        "Google Sheets sync — Android only.",
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = TrackerColors.Ink2.copy(alpha = 0.5f),
    )
}