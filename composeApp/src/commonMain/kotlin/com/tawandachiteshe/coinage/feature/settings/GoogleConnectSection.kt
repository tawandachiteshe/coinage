package com.tawandachiteshe.coinage.feature.settings

import androidx.compose.runtime.Composable
import com.tawandachiteshe.coinage.domain.repository.GoogleAuthRepository

@Composable
expect fun GoogleConnectSection(repository: GoogleAuthRepository)