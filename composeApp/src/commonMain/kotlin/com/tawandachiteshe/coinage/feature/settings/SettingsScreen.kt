package com.tawandachiteshe.coinage.feature.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tawandachiteshe.coinage.domain.repository.GoogleAuthRepository
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.tawandachiteshe.coinage.ui.components.PageHeader
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.TrackerDialog
import com.tawandachiteshe.coinage.ui.components.TrackerScaffold
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val CURRENCIES = listOf(
    "USD" to "$", "EUR" to "€", "GBP" to "£", "ZAR" to "R",
    "CAD" to "C$", "AUD" to "A$", "JPY" to "¥", "NGN" to "₦",
    "INR" to "₹", "BRL" to "R$", "KES" to "KSh", "GHS" to "₵",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
    googleAuthRepository: GoogleAuthRepository = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var theme by remember { mutableStateOf("Tropicana") }
    var voice by remember { mutableStateOf("Wise sibling") }
    var biometric by remember { mutableStateOf(true) }
    var aiEnabled by remember { mutableStateOf(false) }
    var roundUpEnabled by remember { mutableStateOf(true) }

    TrackerScaffold(onBack = onBack) {
        PageHeader(
            eyebrow = "Settings",
            title = "Make it",
            italicWord = "yours.",
            accent = TrackerColors.Grape,
            kicker = "Small knobs. Big feels.",
        )

        // Theme picker
        Spacer(Modifier.height(14.dp))
        StickerCard(
            bgColor = TrackerColors.PaperWhite,
            modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
            cornerRadius = 16.dp,
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Theme".uppercase(), fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.4.sp, color = TrackerColors.Ink2.copy(alpha = 0.7f))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Tropicana"  to listOf(TrackerColors.Tangerine, TrackerColors.Grape, TrackerColors.Butter, TrackerColors.Sky),
                        "Sour candy" to listOf(Color(0xFFc8ff3d), Color(0xFFff5bb0), Color(0xFF3b6dff), Color(0xFFf6f1e6)),
                        "Soft serve" to listOf(Color(0xFFffb59c), Color(0xFF7ce0c0), Color(0xFFc9b8ff), TrackerColors.Paper),
                    ).forEach { (name, cols) ->
                        val isActive = theme == name
                        Box(
                            modifier = Modifier
                                .then(if (isActive) Modifier.popShadow(cornerRadius = 12.dp, offsetX = 3.dp, offsetY = 4.dp)
                                      else Modifier.popShadow(cornerRadius = 12.dp, offsetX = 1.5.dp, offsetY = 2.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(TrackerColors.Paper, RoundedCornerShape(12.dp))
                                .border(if (isActive) 2.dp else 1.5.dp, TrackerColors.Ink, RoundedCornerShape(12.dp))
                                .clickable { theme = name }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    modifier = Modifier
                                        .size(76.dp, 18.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(1.dp, TrackerColors.Ink, RoundedCornerShape(4.dp)),
                                ) {
                                    cols.forEach { c -> Box(modifier = Modifier.weight(1f).height(18.dp).background(c)) }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
                            }
                        }
                    }
                }
            }
        }

        // Currency picker
        Spacer(Modifier.height(20.dp))
        SectionLabel("Default currency")
        StickerCard(
            bgColor = TrackerColors.PaperWhite,
            modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
            cornerRadius = 16.dp,
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "Prices and totals will show in this currency.",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.3.sp,
                    color = TrackerColors.Ink2.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CURRENCIES.forEach { (code, symbol) ->
                        val isSelected = code == state.baseCurrencyCode
                        Box(
                            modifier = Modifier
                                .then(if (isSelected) Modifier.popShadow(cornerRadius = 999.dp, offsetX = 2.dp, offsetY = 2.dp) else Modifier)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isSelected) TrackerColors.Butter else TrackerColors.Paper2, RoundedCornerShape(999.dp))
                                .border(if (isSelected) 1.8.dp else 1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                                .clickable { viewModel.onAction(SettingsAction.OnCurrencyChange(code)) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(symbol, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                Text(code, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink)
                            }
                        }
                    }
                }
            }
        }

        // General section
        Spacer(Modifier.height(20.dp))
        SectionLabel("General")
        Column(modifier = Modifier.padding(horizontal = 22.dp)) {
            SettingsRow(label = "Week starts on") {
                PillGroup(selected = "Mon", options = listOf("Sun", "Mon"), color = TrackerColors.Sky) {}
            }
            SettingsRow(label = "Round-up saves", hint = "dump extra cents into your top goal") {
                TrackerToggle(on = roundUpEnabled, color = TrackerColors.Tangerine) { roundUpEnabled = !roundUpEnabled }
            }
            SettingsRow(label = "Voice", hint = "how Tracker talks to you") {
                PillGroup(selected = voice, options = listOf("Hype", "Wise sibling", "Deadpan"), color = TrackerColors.Coral) { voice = it }
            }
        }

        // Privacy section
        Spacer(Modifier.height(22.dp))
        SectionLabel("Privacy & sync")
        Column(modifier = Modifier.padding(horizontal = 22.dp)) {
            SettingsRow(label = "Unlock with Face ID", hint = "required on cold launch") {
                TrackerToggle(on = biometric, color = TrackerColors.Mint) { biometric = !biometric }
            }
            SettingsRow(label = "AI assistant", hint = "auto-categorize · monthly write-up") {
                TrackerToggle(on = aiEnabled, color = TrackerColors.Grape) { aiEnabled = !aiEnabled }
            }
        }

        // Google Drive
        Spacer(Modifier.height(20.dp))
        SectionLabel("Google Drive")
        GoogleConnectSection(googleAuthRepository, state, viewModel::onAction)

        // Danger zone
        Spacer(Modifier.height(22.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .fillMaxWidth()
                .popShadow(cornerRadius = 14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(TrackerColors.Ink)
                .border(1.8.dp, TrackerColors.Ink, RoundedCornerShape(14.dp))
                .padding(14.dp),
        ) {
            Column {
                Text("Danger zone".uppercase(), fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.4.sp, color = TrackerColors.Butter)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("Export data", "Reset month", "Delete everything").forEachIndexed { i, label ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (i == 2) TrackerColors.Cherry else TrackerColors.Paper)
                                .border(1.4.dp, TrackerColors.Paper, RoundedCornerShape(999.dp))
                                .padding(horizontal = 11.dp, vertical = 6.dp),
                        ) {
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (i == 2) TrackerColors.Paper else TrackerColors.Ink)
                        }
                    }
                }
            }
        }

        // Sheet URL dialog
        state.sheetUrl?.let { url ->
            val clipboard = LocalClipboardManager.current
            TrackerDialog(
                title = "Synced to Sheets",
                onDismissRequest = { viewModel.onAction(SettingsAction.DismissSheetUrl) },
                confirmLabel = "Copy link",
                confirmColor = TrackerColors.Mint,
                onConfirm = {
                    clipboard.setText(AnnotatedString(url))
                    viewModel.onAction(SettingsAction.DismissSheetUrl)
                },
                dismissLabel = "Done",
                onDismiss = { viewModel.onAction(SettingsAction.DismissSheetUrl) },
            ) {
                Text(
                    text = url,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TrackerColors.Ink2,
                    lineHeight = 16.sp,
                )
            }
        }

        // Footer
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Tracker 1.0 · build 26.05 · made with care",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            color = TrackerColors.Ink2.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label.uppercase(),
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 4.dp),
        fontSize = 10.5.sp,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.4.sp,
        color = TrackerColors.Ink2.copy(alpha = 0.65f),
    )
}

@Composable
private fun SettingsRow(
    label: String,
    hint: String? = null,
    control: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
            if (hint != null) Text(hint, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp, color = TrackerColors.Ink2.copy(alpha = 0.6f), modifier = Modifier.padding(top = 1.dp))
        }
        Spacer(Modifier.width(12.dp))
        control()
    }
    val dashColor = TrackerColors.Ink.copy(alpha = 0.18f)
    Canvas(modifier = Modifier.fillMaxWidth().height(1.3.dp)) {
        drawLine(
            color = dashColor,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.3.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
        )
    }
}

@Composable
private fun TrackerToggle(on: Boolean, color: Color, onTap: () -> Unit) {
    val thumbX by animateDpAsState(targetValue = if (on) 24.dp else 2.dp, animationSpec = tween(180), label = "thumb")
    val bgColor by animateColorAsState(targetValue = if (on) color else TrackerColors.Paper2, animationSpec = tween(180), label = "bg")
    Box(
        modifier = Modifier
            .size(48.dp, 28.dp)
            .popShadow(cornerRadius = 999.dp, offsetX = 2.dp, offsetY = 2.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .border(1.6.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
            .clickable(onClick = onTap),
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .offset(x = thumbX, y = 2.dp)
                .clip(CircleShape)
                .background(TrackerColors.Paper, CircleShape)
                .border(1.4.dp, TrackerColors.Ink, CircleShape),
        )
    }
}

@Composable
private fun PillGroup(selected: String, options: List<String>, color: Color, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { opt ->
            val isSelected = opt == selected
            Box(
                modifier = Modifier
                    .then(if (isSelected) Modifier.popShadow(cornerRadius = 999.dp, offsetX = 1.5.dp, offsetY = 2.dp) else Modifier)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isSelected) color else TrackerColors.Paper)
                    .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            ) {
                Text(opt, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
            }
        }
    }
}