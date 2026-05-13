package com.tawandachiteshe.coinage.feature.debt

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tawandachiteshe.coinage.ui.components.PageHeader
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.TrackerTextField
import com.tawandachiteshe.coinage.ui.components.TrackerScaffold
import com.tawandachiteshe.coinage.ui.components.TrackerTab
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import com.tawandachiteshe.coinage.ui.theme.TrackerIcons
import org.koin.compose.viewmodel.koinViewModel

private val DEBT_COLORS = listOf(TrackerColors.Cherry, TrackerColors.Grape, TrackerColors.Coral, TrackerColors.Sky)
private val DEBT_TILTS  = listOf(-0.8f, 0.6f, -1.2f, 0.4f)

@Composable
fun DebtScreen(
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
    viewModel: DebtViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val paidPct = if (state.debts.isNotEmpty()) {
        val totalPrincipal = state.debts.sumOf { it.principal }
        val totalPaid = state.debts.sumOf { it.principal - it.currentBalance }
        if (totalPrincipal > 0) ((totalPaid / totalPrincipal) * 100).toInt() else 0
    } else 0
    val totalOwed = state.totalOwed
    val totalOrig = state.debts.sumOf { it.principal }
    var showAddDialog by remember { mutableStateOf(false) }

    TrackerScaffold(activeTab = TrackerTab.Debt, onTabClick = onTabClick, onAddClick = onAddClick) {
            PageHeader(
                eyebrow = "$paidPct% climbed",
                title = "The",
                italicWord = "mountain.",
                accent = TrackerColors.Cherry,
                kicker = "One step at a time. Smallest first — momentum compounds.",
            )

            // Mountain hero card
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 22.dp)
                    .fillMaxWidth()
                    .popShadow(cornerRadius = 22.dp, offsetX = 4.dp, offsetY = 5.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(TrackerColors.Ink, RoundedCornerShape(22.dp))
                    .padding(18.dp),
            ) {
                Column {
                    Text("Total left to climb", fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.4.sp, color = TrackerColors.Butter)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Paper.copy(alpha = 0.7f))
                        Text(totalOwed.fmtWhole(), fontSize = 48.sp, fontWeight = FontWeight.Bold, lineHeight = 46.sp, color = TrackerColors.Paper)
                    }
                    Text("down from \$${totalOrig.fmtWhole()} · keep going.", fontSize = 15.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Paper.copy(alpha = 0.75f))

                    Spacer(Modifier.height(12.dp))
                    MountainVisual(paidPct = paidPct, modifier = Modifier.fillMaxWidth().height(100.dp))

                    Spacer(Modifier.height(6.dp))
                    // Progress bar
                    Box(
                        modifier = Modifier.fillMaxWidth().height(12.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(TrackerColors.Paper.copy(alpha = 0.12f))
                            .border(1.4.dp, TrackerColors.Paper, RoundedCornerShape(999.dp)),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(fraction = paidPct / 100f).height(12.dp)
                                .background(TrackerColors.Mint),
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("\$0 paid", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Paper.copy(alpha = 0.55f))
                        Text("$paidPct%", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Paper.copy(alpha = 0.55f))
                        Text("\$${totalOrig.fmtWhole()}", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Paper.copy(alpha = 0.55f))
                    }
                }
            }

            // Snowball plan chip
            Spacer(Modifier.height(20.dp))
            StickerCard(
                bgColor = TrackerColors.Mint,
                modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth().rotate(-0.6f),
                cornerRadius = 16.dp, borderWidth = 1.8.dp,
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TrackerColors.Paper, RoundedCornerShape(10.dp))
                            .border(1.6.dp, TrackerColors.Ink, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) { Icon(TrackerIcons.Snowflake, contentDescription = null, modifier = Modifier.size(18.dp), tint = TrackerColors.Ink) }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (state.isSnowball) "Snowball plan · pay smallest first" else "Avalanche plan · pay highest APR first",
                            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink,
                        )
                        Text(
                            "${state.debts.size} debt${if (state.debts.size != 1) "s" else ""} tracked",
                            fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp, color = TrackerColors.Ink2.copy(alpha = 0.65f),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(TrackerColors.Paper)
                            .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                            .clickable { viewModel.onAction(DebtAction.OnToggleOrder) }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) { Text("toggle", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink) }
                }
            }

            // Debt cards
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (state.debts.isEmpty() && !state.isLoading) {
                    Text(
                        text = "No debts yet — add one below.",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TrackerColors.Ink2.copy(alpha = 0.55f),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
                state.debts.forEachIndexed { i, debt ->
                    val color = DEBT_COLORS[i % DEBT_COLORS.size]
                    val tilt = DEBT_TILTS[i % DEBT_TILTS.size]
                    val almostDone = debt.pctPaid >= 80f
                    StickerCard(
                        bgColor = TrackerColors.PaperWhite,
                        modifier = Modifier.fillMaxWidth(),
                        tilt = tilt,
                        cornerRadius = 16.dp,
                        borderWidth = 1.8.dp,
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            if (almostDone) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .rotate(-8f)
                                        .border(1.5.dp, TrackerColors.Mint, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                ) {
                                    Text("almost!", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = TrackerColors.Mint, letterSpacing = 1.2.sp)
                                }
                            }
                            Row {
                                Box(modifier = Modifier.width(8.dp).height(120.dp).clip(RoundedCornerShape(4.dp)).background(color).border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(4.dp)))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(debt.creditorName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                    Text("${debt.debtType} · ${debt.interestRate}% APR", fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp, color = TrackerColors.Ink2.copy(alpha = 0.65f))
                                    Spacer(Modifier.height(10.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("\$${debt.currentBalance.fmtWhole()}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                        Spacer(Modifier.width(8.dp))
                                        Text("of \$${debt.principal.fmtWhole()} left", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.55f))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(10.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(TrackerColors.Paper2)
                                            .border(1.3.dp, TrackerColors.Ink, RoundedCornerShape(999.dp)),
                                    ) {
                                        Box(Modifier.fillMaxWidth(debt.pctPaid / 100f).height(10.dp).background(color))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row {
                                            Text("min: ", fontSize = 13.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Ink2)
                                            Text("\$${debt.minimumPayment.fmtWhole()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(TrackerColors.Butter)
                                                .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                                                .clickable { viewModel.onAction(DebtAction.OnMakePayment(debt.id, debt.minimumPayment)) }
                                                .padding(horizontal = 11.dp, vertical = 5.dp),
                                        ) {
                                            Text("Pay now", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Add new debt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .border(2.dp, TrackerColors.Ink, RoundedCornerShape(16.dp))
                        .clickable { showAddDialog = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("+", fontSize = 22.sp, lineHeight = 22.sp, color = TrackerColors.Ink2)
                        Spacer(Modifier.width(10.dp))
                        Text("Add a debt", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink2)
                    }
                }
            }

        if (showAddDialog) {
            AddDebtDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { creditor, type, principal, apr, minPayment ->
                    viewModel.onAction(DebtAction.OnCreateDebt(
                        creditor = creditor,
                        debtType = type,
                        principal = principal,
                        interestRate = apr,
                        minimumPayment = minPayment,
                    ))
                    showAddDialog = false
                },
            )
        }
    }
}

@Composable
private fun AddDebtDialog(
    onDismiss: () -> Unit,
    onConfirm: (creditor: String, type: String, principal: Double, apr: Double, minPayment: Double) -> Unit,
) {
    var creditor by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("LOAN") }
    var principal by remember { mutableStateOf("") }
    var apr by remember { mutableStateOf("") }
    var minPayment by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        StickerCard(bgColor = TrackerColors.Paper, cornerRadius = 20.dp, shadowX = 4.dp, shadowY = 5.dp) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("New debt", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                TrackerTextField(value = creditor, onValueChange = { creditor = it }, label = "Creditor name", modifier = Modifier.fillMaxWidth())
                TrackerTextField(value = type, onValueChange = { type = it }, label = "Type (e.g. LOAN, CARD)", modifier = Modifier.fillMaxWidth())
                TrackerTextField(value = principal, onValueChange = { principal = it }, label = "Balance", leadingText = "$", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                TrackerTextField(value = apr, onValueChange = { apr = it }, label = "APR %", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                TrackerTextField(value = minPayment, onValueChange = { minPayment = it }, label = "Min payment", leadingText = "$", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp)) { Text("Cancel", color = TrackerColors.Ink2) }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TrackerColors.Ink)
                            .clickable {
                                onConfirm(
                                    creditor,
                                    type.ifBlank { "LOAN" },
                                    principal.toDoubleOrNull() ?: 0.0,
                                    apr.toDoubleOrNull() ?: 0.0,
                                    minPayment.toDoubleOrNull() ?: 0.0,
                                )
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) { Text("Save", color = TrackerColors.Paper, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun MountainVisual(paidPct: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(TrackerIcons.Mountain, contentDescription = null, modifier = Modifier.size(16.dp), tint = TrackerColors.Butter)
            Text(
                text = "$paidPct% of the mountain climbed",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                color = TrackerColors.Butter,
            )
        }
    }
}

private fun Double.fmtWhole(): String {
    val l = kotlin.math.abs(toLong())
    return if (l >= 1000) "${l / 1000},${(l % 1000).toString().padStart(3, '0')}" else l.toString()
}