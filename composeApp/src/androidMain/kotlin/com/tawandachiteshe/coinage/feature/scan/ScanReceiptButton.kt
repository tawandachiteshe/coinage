package com.tawandachiteshe.coinage.feature.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.tawandachiteshe.coinage.ui.components.TrackerDialog
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import kotlinx.coroutines.launch
import java.io.File

@Composable
actual fun ScanReceiptButton(
    modifier: Modifier,
    onScanned: (ScannedReceipt?) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var showSourcePicker by remember { mutableStateOf(false) }
    var pendingCamera by remember { mutableStateOf(false) }

    val photoFile = remember { File(context.cacheDir, "receipt_scan.jpg") }
    val photoUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
    }

    fun processUri(uri: android.net.Uri) {
        isProcessing = true
        scope.launch {
            try {
                val ocr = OcrProcessor(context).processUri(uri)
                onScanned(ReceiptParser.parse(ocr.rawText, ocr.emphasizedLineTexts))
            } catch (_: Exception) {
                onScanned(null)
            } finally {
                isProcessing = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) processUri(photoUri)
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { processUri(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && pendingCamera) {
            pendingCamera = false
            cameraLauncher.launch(photoUri)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(TrackerColors.Sky.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
            .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
            .clickable(enabled = !isProcessing) { showSourcePicker = true }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isProcessing) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp, color = TrackerColors.Ink)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Scan receipt", modifier = Modifier.size(14.dp), tint = TrackerColors.Ink)
                Text("Scan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
            }
        }
    }

    if (showSourcePicker) {
        TrackerDialog(
            title = "Scan a receipt or ticket",
            confirmLabel = "Camera",
            confirmColor = TrackerColors.Sky,
            onConfirm = {
                showSourcePicker = false
                val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    cameraLauncher.launch(photoUri)
                } else {
                    pendingCamera = true
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            dismissLabel = "Gallery",
            onDismiss = {
                showSourcePicker = false
                galleryLauncher.launch("image/*")
            },
            onDismissRequest = { showSourcePicker = false },
        )
    }
}