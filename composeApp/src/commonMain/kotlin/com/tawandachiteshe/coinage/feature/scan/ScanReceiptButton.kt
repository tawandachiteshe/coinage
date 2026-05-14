package com.tawandachiteshe.coinage.feature.scan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ScanReceiptButton(
    modifier: Modifier = Modifier,
    onScanned: (ScannedReceipt?) -> Unit,
)
