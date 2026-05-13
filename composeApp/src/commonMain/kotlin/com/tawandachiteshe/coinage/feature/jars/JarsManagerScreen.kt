package com.tawandachiteshe.coinage.feature.jars

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tawandachiteshe.coinage.ui.components.PageHeader
import com.tawandachiteshe.coinage.ui.components.ProgressJar
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.StripedProgressBar
import com.tawandachiteshe.coinage.ui.components.TrackerTextField
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import com.tawandachiteshe.coinage.ui.theme.TrackerIcons
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun JarsManagerScreen(
    onBack: () -> Unit,
    viewModel: JarsManagerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val jars = state.jars

    var showAddDialog by remember { mutableStateOf(false) }
    var editBudgetJar by remember { mutableStateOf<JarUi?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrackerColors.Paper),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 40.dp),
        ) {
            // Back button
            Row(modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)) {
                Text(
                    text = "← back",
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(8.dp),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp,
                    color = TrackerColors.Ink2,
                )
            }

            // Page header
            PageHeader(
                eyebrow = "${jars.size} jars",
                title = "Your",
                italicWord = "jars.",
                accent = TrackerColors.Tangerine,
                kicker = "Set a limit. Track what fills — and what spills.",
            )

            Spacer(Modifier.height(20.dp))

            // Jar cards
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                jars.forEach { jar ->
                    JarCard(
                        jar = jar,
                        onEditBudget = { editBudgetJar = jar },
                        onDelete = { viewModel.onAction(JarsAction.OnDeleteJar(jar.id)) },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Add new jar button
            Box(
                modifier = Modifier
                    .padding(horizontal = 22.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 2.dp,
                        color = TrackerColors.Ink.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(14.dp),
                    )
                    .clickable { showAddDialog = true }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = TrackerIcons.Add,
                        contentDescription = "Add jar",
                        modifier = Modifier.size(18.dp),
                        tint = TrackerColors.Ink2,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Add new jar",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp,
                        color = TrackerColors.Ink2,
                    )
                }
            }
        }
    }

    // Edit budget dialog
    editBudgetJar?.let { jar ->
        EditBudgetDialog(
            jar = jar,
            onConfirm = { newBudget ->
                viewModel.onAction(JarsAction.OnUpdateBudget(jar.id, newBudget))
                editBudgetJar = null
            },
            onDismiss = { editBudgetJar = null },
        )
    }

    // Add jar dialog
    if (showAddDialog) {
        AddJarDialog(
            onConfirm = { name, icon, colorHex, budget ->
                viewModel.onAction(JarsAction.OnCreateJar(name, icon, colorHex, budget))
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
private fun JarCard(
    jar: JarUi,
    onEditBudget: () -> Unit,
    onDelete: () -> Unit,
) {
    val jarColor = parseHexColor(jar.colorHex)
    val isOverspent = jar.budgetLimit > 0 && jar.spent > jar.budgetLimit
    val barColor = if (isOverspent) TrackerColors.Cherry else jarColor
    val pct = if (jar.budgetLimit > 0) (jar.spent / jar.budgetLimit * 100f).toFloat().coerceIn(0f, 100f) else 0f

    StickerCard(
        bgColor = TrackerColors.PaperWhite,
        tilt = jar.tilt,
        cornerRadius = 16.dp,
        borderWidth = 1.8.dp,
        shadowX = 3.dp,
        shadowY = 4.dp,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left color bar
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(120.dp)
                    .background(jarColor)
                    .border(1.dp, TrackerColors.Ink),
            )

            Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                // Icon + name row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(jarColor)
                            .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = TrackerIcons.fromKey(jar.icon),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = TrackerColors.Ink,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = jar.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrackerColors.Ink,
                        )
                        Text(
                            text = jar.icon,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp,
                            color = TrackerColors.Ink2.copy(alpha = 0.55f),
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Spending amounts
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "\$${jar.spent.formatWhole()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverspent) TrackerColors.Cherry else TrackerColors.Ink,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (jar.budgetLimit > 0) "/ \$${jar.budgetLimit.formatWhole()}" else "/ no limit",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TrackerColors.Ink2.copy(alpha = 0.6f),
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Progress bar
                if (jar.budgetLimit > 0) {
                    StripedProgressBar(
                        pct = pct,
                        color = barColor,
                        modifier = Modifier.fillMaxWidth(),
                        height = 12.dp,
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Action chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Edit limit chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(TrackerColors.Butter)
                            .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                            .clickable { onEditBudget() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "Edit limit",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            color = TrackerColors.Ink,
                        )
                    }

                    // Delete chip (non-default jars only)
                    if (!jar.isDefault) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(TrackerColors.Cherry.copy(alpha = 0.15f))
                                .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                                .clickable { onDelete() }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                imageVector = TrackerIcons.Other,
                                contentDescription = "Delete jar",
                                modifier = Modifier.size(14.dp),
                                tint = TrackerColors.Cherry,
                            )
                        }
                    }
                }
            }

            // Mini progress jar on the right
            Box(modifier = Modifier.padding(end = 12.dp, top = 12.dp, bottom = 12.dp)) {
                ProgressJar(
                    pct = pct,
                    color = barColor,
                    height = 72.dp,
                    width = 24.dp,
                )
            }
        }
    }
}

@Composable
private fun EditBudgetDialog(
    jar: JarUi,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var budgetText by remember { mutableStateOf(if (jar.budgetLimit > 0) jar.budgetLimit.toInt().toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit budget for ${jar.name}",
                fontWeight = FontWeight.Bold,
                color = TrackerColors.Ink,
            )
        },
        text = {
            Column {
                Text(
                    text = "Set a monthly spending limit (0 = no limit)",
                    fontSize = 13.sp,
                    color = TrackerColors.Ink2,
                )
                Spacer(Modifier.height(12.dp))
                TrackerTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = "Budget",
                    leadingText = "$",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = budgetText.toDoubleOrNull() ?: 0.0
                    onConfirm(amount)
                },
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun AddJarDialog(
    onConfirm: (name: String, icon: String, colorHex: String, budget: Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var iconKey by remember { mutableStateOf("other") }
    var colorHex by remember { mutableStateOf("#ff7a2b") }
    var budgetText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New jar",
                fontWeight = FontWeight.Bold,
                color = TrackerColors.Ink,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TrackerTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name",
                    modifier = Modifier.fillMaxWidth(),
                )
                TrackerTextField(
                    value = iconKey,
                    onValueChange = { iconKey = it },
                    label = "Icon key (e.g. coffee, car)",
                    modifier = Modifier.fillMaxWidth(),
                )
                TrackerTextField(
                    value = colorHex,
                    onValueChange = { colorHex = it },
                    label = "Color (#hex)",
                    modifier = Modifier.fillMaxWidth(),
                )
                TrackerTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = "Monthly budget",
                    leadingText = "$",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val budget = budgetText.toDoubleOrNull() ?: 0.0
                        val hex = colorHex.takeIf { it.startsWith("#") } ?: "#ff7a2b"
                        onConfirm(name.trim(), iconKey.trim().ifBlank { "other" }, hex, budget)
                    }
                },
            ) {
                Text("Create", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private fun Double.formatWhole(): String {
    val l = kotlin.math.abs(toLong())
    return if (l >= 1000) "${l / 1000},${(l % 1000).toString().padStart(3, '0')}" else l.toString()
}

private fun parseHexColor(hex: String): Color {
    val clean = hex.trimStart('#').padStart(6, '0')
    return try {
        Color(
            red   = clean.substring(0, 2).toInt(16) / 255f,
            green = clean.substring(2, 4).toInt(16) / 255f,
            blue  = clean.substring(4, 6).toInt(16) / 255f,
        )
    } catch (_: Exception) {
        TrackerColors.Grape
    }
}