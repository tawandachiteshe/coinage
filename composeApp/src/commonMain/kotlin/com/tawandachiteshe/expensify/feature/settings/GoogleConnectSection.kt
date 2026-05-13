package com.tawandachiteshe.expensify.feature.settings

import androidx.compose.runtime.Composable
import com.tawandachiteshe.expensify.domain.repository.GoogleAuthRepository

@Composable
expect fun GoogleConnectSection(repository: GoogleAuthRepository)