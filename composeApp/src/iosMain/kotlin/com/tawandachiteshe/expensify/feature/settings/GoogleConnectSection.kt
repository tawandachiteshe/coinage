package com.tawandachiteshe.expensify.feature.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.tawandachiteshe.expensify.domain.repository.GoogleAuthRepository
import com.tawandachiteshe.expensify.ui.theme.TrackerColors

@Composable
actual fun GoogleConnectSection(repository: GoogleAuthRepository) {
    Text(
        "Google Sheets sync — coming to iOS soon.",
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = TrackerColors.Ink2.copy(alpha = 0.5f),
    )
}