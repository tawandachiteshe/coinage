package com.tawandachiteshe.coinage.feature.debt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.tawandachiteshe.coinage.ui.components.TrackerDialog
import com.tawandachiteshe.coinage.ui.components.TrackerTextField
import com.tawandachiteshe.coinage.ui.components.TrackerScaffold
import com.tawandachiteshe.coinage.ui.components.TrackerTab
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import com.tawandachiteshe.coinage.ui.theme.TrackerIcons
import org.koin.compose.viewmodel.koinViewModel

private val DEBT_COLORS = listOf(TrackerColors.Cherry, TrackerColors.Grape, TrackerColors.Coral, TrackerColors.Sky)
private val DEBT_TILTS  = listOf(-0.8f, 0.6f, -1.2f, 0.4f)
private val IOU_COLORS  = listOf(TrackerColors.Sky, TrackerColors.Mint, TrackerColors.Grape, TrackerColors.Butter)

@Composable
fun DebtScreen(
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
    viewModel: DebtViewModel = koinViewModel(),
    iouViewModel: IouViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val iouState by iouViewModel.state.collectAsStateWithLifecycle()

    var showIous by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var showAddIouDialog by remember { mutableStateOf(false) }
    var iouToDelete by remember { mutableStateOf<IouUi?>(null) }
    var pendingIouPayment by remember { mutableStateOf<Pair<IouUi, Double>?>(null) }
    var pendingDebtPayment by remember { mutableStateOf<Pair<DebtUi, Double>?>(null) }
    var customDebtEntry by remember { mutableStateOf<DebtUi?>(null) }
    var customDebtAmount by remember { mutableStateOf("") }
    var customIouEntry by remember { mutableStateOf<IouUi?>(null) }
    var customIouAmount by remember { mutableStateOf("") }

    val paidPct = if (state.debts.isNotEmpty()) {
        val totalPrincipal = state.debts.sumOf { it.principal }
        val totalPaid = state.debts.sumOf { it.principal - it.currentBalance }
        if (totalPrincipal > 0) ((totalPaid / totalPrincipal) * 100).toInt() else 0
    } else 0
    val totalOwed = state.totalOwed
    val totalOrig = state.debts.sumOf { it.principal }

    TrackerScaffold(activeTab = TrackerTab.Debt, onTabClick = onTabClick, onAddClick = onAddClick) {

        PageHeader(
            eyebrow = if (showIous) "${iouState.ious.size} people owe you" else "$paidPct% climbed",
            title = if (showIous) "Owe" else "The",
            italicWord = if (showIous) "me." else "mountain.",
            accent = if (showIous) TrackerColors.Sky else TrackerColors.Cherry,
            kicker = if (showIous) "Track loans to friends and family. Mark payments as they come in."
                     else "One step at a time. Smallest first — momentum compounds.",
        )

        // Mode toggle — I Owe / Owe Me
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.padding(horizontal = 22.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TrackerColors.Paper2)
                .border(1.6.dp, TrackerColors.Ink, RoundedCornerShape(12.dp))
                .padding(3.dp),
        ) {
            listOf("I Owe" to false, "Owe Me" to true).forEach { (label, isIou) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (showIous == isIou) TrackerColors.Ink else TrackerColors.Paper2)
                        .clickable { showIous = isIou }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (showIous == isIou) TrackerColors.Paper else TrackerColors.Ink2,
                    )
                }
            }
        }

        if (!showIous) {
            // ── I Owe view ─────────────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
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
                    Box(
                        modifier = Modifier.fillMaxWidth().height(12.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(TrackerColors.Paper.copy(alpha = 0.12f))
                            .border(1.4.dp, TrackerColors.Paper, RoundedCornerShape(999.dp)),
                    ) {
                        Box(Modifier.fillMaxWidth(fraction = paidPct / 100f).height(12.dp).background(TrackerColors.Mint))
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("\$0 paid", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Paper.copy(alpha = 0.55f))
                        Text("$paidPct%", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Paper.copy(alpha = 0.55f))
                        Text("\$${totalOrig.fmtWhole()}", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Paper.copy(alpha = 0.55f))
                    }
                }
            }

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

            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (state.debts.isEmpty() && !state.isLoading) {
                    Text("No debts yet — add one below.", fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.55f), modifier = Modifier.padding(vertical = 8.dp))
                }
                state.debts.forEachIndexed { i, debt ->
                    val color = DEBT_COLORS[i % DEBT_COLORS.size]
                    val tilt = DEBT_TILTS[i % DEBT_TILTS.size]
                    val almostDone = debt.pctPaid >= 80f
                    StickerCard(bgColor = TrackerColors.PaperWhite, modifier = Modifier.fillMaxWidth(), tilt = tilt, cornerRadius = 16.dp, borderWidth = 1.8.dp) {
                        Box(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            if (almostDone) {
                                Box(modifier = Modifier.align(Alignment.TopEnd).rotate(-8f).border(1.5.dp, TrackerColors.Mint, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
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
                                    Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(999.dp)).background(TrackerColors.Paper2).border(1.3.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))) {
                                        Box(Modifier.fillMaxWidth(debt.pctPaid / 100f).height(10.dp).background(color))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(TrackerColors.Butter).border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                                                .clickable { pendingDebtPayment = debt to debt.minimumPayment }
                                                .padding(horizontal = 11.dp, vertical = 5.dp),
                                        ) { Text("Min \$${debt.minimumPayment.fmtWhole()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink) }
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(TrackerColors.Paper2).border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                                                .clickable { customDebtEntry = debt; customDebtAmount = "" }
                                                .padding(horizontal = 11.dp, vertical = 5.dp),
                                        ) { Text("Custom…", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink) }
                                    }
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth().height(64.dp).border(2.dp, TrackerColors.Ink, RoundedCornerShape(16.dp)).clickable { showAddDebtDialog = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("+", fontSize = 22.sp, lineHeight = 22.sp, color = TrackerColors.Ink2)
                        Spacer(Modifier.width(10.dp))
                        Text("Add a debt", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink2)
                    }
                }
            }

        } else {
            // ── Owe Me view ────────────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
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
                    Text("Total outstanding", fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.4.sp, color = TrackerColors.Sky)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Paper.copy(alpha = 0.7f))
                        Text(iouState.totalOutstanding.fmtWhole(), fontSize = 48.sp, fontWeight = FontWeight.Bold, lineHeight = 46.sp, color = TrackerColors.Paper)
                    }
                    Text(
                        "${iouState.ious.count { !it.isSettled }} outstanding · ${iouState.ious.count { it.isSettled }} settled",
                        fontSize = 15.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Paper.copy(alpha = 0.75f),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (iouState.ious.isEmpty() && !iouState.isLoading) {
                    Text("Nobody owes you yet — add one below.", fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.55f), modifier = Modifier.padding(vertical = 8.dp))
                }
                iouState.ious.forEachIndexed { i, iou ->
                    val color = IOU_COLORS[i % IOU_COLORS.size]
                    val tilt = DEBT_TILTS[i % DEBT_TILTS.size]
                    StickerCard(
                        bgColor = if (iou.isSettled) TrackerColors.Paper2 else TrackerColors.PaperWhite,
                        modifier = Modifier.fillMaxWidth(),
                        tilt = tilt,
                        cornerRadius = 16.dp,
                        borderWidth = 1.8.dp,
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            if (iou.isSettled) {
                                Box(modifier = Modifier.align(Alignment.TopEnd).rotate(-8f).border(1.5.dp, TrackerColors.Mint, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                    Text("settled ✓", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = TrackerColors.Mint, letterSpacing = 1.2.sp)
                                }
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                                            .background(color).border(1.6.dp, TrackerColors.Ink, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            iou.personName.take(1).uppercase(),
                                            fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TrackerColors.Ink,
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(iou.personName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                        if (iou.categoryName != null) Text(iou.categoryName, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Sky.copy(alpha = 0.85f), fontWeight = FontWeight.SemiBold)
                                        if (iou.notes != null) Text(iou.notes, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.6f))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("\$${iou.outstanding.fmtWhole()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (iou.isSettled) TrackerColors.Mint else TrackerColors.Ink)
                                        Text("of \$${iou.amount.fmtWhole()}", fontSize = 10.sp, color = TrackerColors.Ink2.copy(alpha = 0.5f))
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(999.dp)).background(TrackerColors.Paper2).border(1.3.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))) {
                                    Box(Modifier.fillMaxWidth(iou.pctRepaid / 100f).height(10.dp).background(color))
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    if (!iou.isSettled) {
                                        listOf(10, 25, 50).forEach { amt ->
                                            val receivable = amt.toDouble() <= iou.outstanding
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(999.dp))
                                                    .background(
                                                        if (receivable) TrackerColors.Paper2 else TrackerColors.Paper2.copy(alpha = 0.4f),
                                                        RoundedCornerShape(999.dp),
                                                    )
                                                    .border(
                                                        1.4.dp,
                                                        if (receivable) TrackerColors.Ink else TrackerColors.Ink.copy(alpha = 0.25f),
                                                        RoundedCornerShape(999.dp),
                                                    )
                                                    .clickable(enabled = receivable) { pendingIouPayment = iou to amt.toDouble() }
                                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                            ) {
                                                Text(
                                                    "\$${amt} back",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (receivable) TrackerColors.Ink else TrackerColors.Ink.copy(alpha = 0.3f),
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(TrackerColors.Sky.copy(alpha = 0.18f))
                                                .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                                                .clickable { customIouEntry = iou; customIouAmount = "" }
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                        ) { Text("Custom…", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink) }
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Box(
                                        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                                            .background(TrackerColors.Cherry.copy(alpha = 0.12f))
                                            .clickable { iouToDelete = iou },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(TrackerIcons.Trash, contentDescription = "Delete", modifier = Modifier.size(14.dp), tint = TrackerColors.Cherry)
                                    }
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().height(64.dp).border(2.dp, TrackerColors.Ink, RoundedCornerShape(16.dp)).clickable { showAddIouDialog = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("+", fontSize = 22.sp, lineHeight = 22.sp, color = TrackerColors.Ink2)
                        Spacer(Modifier.width(10.dp))
                        Text("Add someone who owes you", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink2)
                    }
                }
            }
        }

        if (showAddDebtDialog) {
            AddDebtDialog(
                onDismiss = { showAddDebtDialog = false },
                onConfirm = { creditor, type, principal, apr, minPayment ->
                    viewModel.onAction(DebtAction.OnCreateDebt(creditor = creditor, debtType = type, principal = principal, interestRate = apr, minimumPayment = minPayment))
                    showAddDebtDialog = false
                },
            )
        }

        if (showAddIouDialog) {
            AddIouDialog(
                categories = iouState.incomeCats,
                onDismiss = { showAddIouDialog = false },
                onConfirm = { name, amount, notes, categoryId ->
                    iouViewModel.onAction(IouAction.OnCreateIou(personName = name, amount = amount, notes = notes, categoryId = categoryId))
                    showAddIouDialog = false
                },
            )
        }

        pendingDebtPayment?.let { (debt, amt) ->
            TrackerDialog(
                title = "Pay \$${amt.fmtWhole()} to ${debt.creditorName}?",
                confirmLabel = "Pay \$${amt.fmtWhole()}",
                confirmColor = TrackerColors.Butter,
                onConfirm = {
                    viewModel.onAction(DebtAction.OnMakePayment(debt.id, amt))
                    pendingDebtPayment = null
                },
                onDismissRequest = { pendingDebtPayment = null },
                dismissLabel = "Cancel",
                onDismiss = { pendingDebtPayment = null },
            )
        }

        customDebtEntry?.let { debt ->
            TrackerDialog(
                title = "Custom payment to ${debt.creditorName}",
                confirmLabel = "Pay",
                confirmColor = TrackerColors.Butter,
                onConfirm = {
                    val amt = customDebtAmount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) pendingDebtPayment = debt to amt
                    customDebtEntry = null
                    customDebtAmount = ""
                },
                onDismissRequest = { customDebtEntry = null; customDebtAmount = "" },
                dismissLabel = "Cancel",
                onDismiss = { customDebtEntry = null; customDebtAmount = "" },
                content = {
                    TrackerTextField(
                        value = customDebtAmount,
                        onValueChange = { customDebtAmount = it },
                        label = "Amount",
                        leadingText = "$",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )
        }

        pendingIouPayment?.let { (iou, amt) ->
            TrackerDialog(
                title = "Mark \$${amt.fmtWhole()} received from ${iou.personName}?",
                confirmLabel = "\$${amt.fmtWhole()} received",
                confirmColor = TrackerColors.Sky,
                onConfirm = {
                    iouViewModel.onAction(IouAction.OnRecordPayment(iou.id, amt))
                    pendingIouPayment = null
                },
                onDismissRequest = { pendingIouPayment = null },
                dismissLabel = "Cancel",
                onDismiss = { pendingIouPayment = null },
            )
        }

        customIouEntry?.let { iou ->
            TrackerDialog(
                title = "Custom amount from ${iou.personName}",
                confirmLabel = "Mark received",
                confirmColor = TrackerColors.Sky,
                onConfirm = {
                    val amt = (customIouAmount.toDoubleOrNull() ?: 0.0).coerceAtMost(iou.outstanding)
                    if (amt > 0) pendingIouPayment = iou to amt
                    customIouEntry = null
                    customIouAmount = ""
                },
                onDismissRequest = { customIouEntry = null; customIouAmount = "" },
                dismissLabel = "Cancel",
                onDismiss = { customIouEntry = null; customIouAmount = "" },
                content = {
                    TrackerTextField(
                        value = customIouAmount,
                        onValueChange = { customIouAmount = it },
                        label = "Amount (max \$${iou.outstanding.fmtWhole()})",
                        leadingText = "$",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )
        }

        iouToDelete?.let { iou ->
            TrackerDialog(
                title = "Delete IOU for ${iou.personName}?",
                confirmLabel = "Delete",
                confirmColor = TrackerColors.Cherry,
                onConfirm = {
                    iouViewModel.onAction(IouAction.OnDeleteIou(iou.id))
                    iouToDelete = null
                },
                onDismissRequest = { iouToDelete = null },
                dismissLabel = "Cancel",
                onDismiss = { iouToDelete = null },
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
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(TrackerColors.Ink)
                            .clickable { onConfirm(creditor, type.ifBlank { "LOAN" }, principal.toDoubleOrNull() ?: 0.0, apr.toDoubleOrNull() ?: 0.0, minPayment.toDoubleOrNull() ?: 0.0) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) { Text("Save", color = TrackerColors.Paper, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun AddIouDialog(
    categories: List<IouCategoryOption>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double, notes: String?, categoryId: String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        StickerCard(bgColor = TrackerColors.Paper, cornerRadius = 20.dp, shadowX = 4.dp, shadowY = 5.dp) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Who owes you?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                TrackerTextField(value = name, onValueChange = { name = it }, label = "Person's name", modifier = Modifier.fillMaxWidth())
                TrackerTextField(value = amount, onValueChange = { amount = it }, label = "Amount lent", leadingText = "$", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                TrackerTextField(value = notes, onValueChange = { notes = it }, label = "What for? (optional)", modifier = Modifier.fillMaxWidth())
                if (categories.isNotEmpty()) {
                    Text("Income category", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.65f))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            val selected = cat.id == selectedCategoryId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (selected) TrackerColors.Sky else TrackerColors.Paper2)
                                    .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                                    .clickable { selectedCategoryId = if (selected) null else cat.id }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(cat.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp)) { Text("Cancel", color = TrackerColors.Ink2) }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(TrackerColors.Sky)
                            .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(12.dp))
                            .clickable { onConfirm(name, amount.toDoubleOrNull() ?: 0.0, notes.ifBlank { null }, selectedCategoryId) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) { Text("Add", color = TrackerColors.Ink, fontWeight = FontWeight.Bold) }
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
            Text("$paidPct% of the mountain climbed", fontSize = 14.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Butter)
        }
    }
}

private fun Double.fmtWhole(): String {
    val l = kotlin.math.abs(toLong())
    return if (l >= 1000) "${l / 1000},${(l % 1000).toString().padStart(3, '0')}" else l.toString()
}