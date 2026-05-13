package com.tawandachiteshe.coinage.feature.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.TrackerTextField
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import com.tawandachiteshe.coinage.ui.theme.TrackerIcons
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddScreen(
    initialType: AddType = AddType.Transaction,
    onDismiss: () -> Unit,
    viewModel: AddViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(onBack = onDismiss)

    LaunchedEffect(initialType) {
        viewModel.onAction(AddAction.OnReset(initialType))
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is AddEvent.Saved) onDismiss()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Dim scrim — tap to dismiss
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = onDismiss),
        )

        // Sheet card — outer Column: no scroll; inner Column: scrollable form; save button: always pinned at bottom
        val sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .align(Alignment.BottomCenter)
                .clip(sheetShape)
                .background(TrackerColors.Paper, sheetShape)
                .border(2.dp, TrackerColors.Ink, sheetShape)
                .pointerInput(Unit) {},
        ) {
            // Scrollable form content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(36.dp, 3.dp)
                        .background(TrackerColors.Ink.copy(alpha = 0.2f), RoundedCornerShape(999.dp)),
                )

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("New ", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                        Text(
                            text = state.addType.name.lowercase() + ".",
                            fontSize = 26.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            color = TrackerColors.Tangerine,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(TrackerColors.Paper2, RoundedCornerShape(999.dp))
                            .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(TrackerIcons.X, contentDescription = "Close", modifier = Modifier.size(14.dp), tint = TrackerColors.Ink)
                    }
                }

                // Type picker
                TypePicker(selected = state.addType, onSelect = { viewModel.onAction(AddAction.OnAddTypeChange(it)) })

                // Context-aware form
                when (state.addType) {
                    AddType.Transaction -> TransactionSheetContent(state = state, onAction = viewModel::onAction)
                    AddType.Goal        -> GoalSheetContent(state = state, onAction = viewModel::onAction)
                    AddType.Debt        -> DebtSheetContent(state = state, onAction = viewModel::onAction)
                }

                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        fontSize = 13.sp,
                        color = TrackerColors.Cherry,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            // Save button — pinned, never scrolls away
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TrackerColors.Paper)
                    .padding(horizontal = 22.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
            ) {
                StickerCard(
                    bgColor = TrackerColors.Tangerine,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    borderWidth = 2.dp,
                    shadowX = 4.dp,
                    shadowY = 4.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !state.isLoading) { viewModel.onAction(AddAction.OnSave) }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(TrackerIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = TrackerColors.Ink)
                            Spacer(Modifier.width(8.dp))
                            Text(if (state.isLoading) "Saving…" else "Save", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                        }
                    }
                }
            }
        }
    }
}

// ── Type picker ───────────────────────────────────────────────────────────────

@Composable
private fun TypePicker(selected: AddType, onSelect: (AddType) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(TrackerColors.Paper2, RoundedCornerShape(999.dp))
            .border(1.6.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
            .padding(4.dp),
    ) {
        listOf(AddType.Transaction to "transaction", AddType.Goal to "goal", AddType.Debt to "debt").forEach { (type, label) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected == type) TrackerColors.Ink else Color.Transparent, RoundedCornerShape(999.dp))
                    .clickable { onSelect(type) }
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = if (selected == type) TrackerColors.Paper else TrackerColors.Ink,
                )
            }
        }
    }
}

// ── Transaction sheet ─────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionSheetContent(state: AddState, onAction: (AddAction) -> Unit) {
    // Expense / Income toggle
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(TrackerColors.Paper2, RoundedCornerShape(999.dp))
            .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
            .padding(3.dp),
    ) {
        listOf(TxType.EXPENSE to "expense", TxType.INCOME to "income").forEach { (t, label) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (state.txType == t) if (t == TxType.EXPENSE) TrackerColors.Cherry else TrackerColors.Mint
                        else Color.Transparent,
                        RoundedCornerShape(999.dp),
                    )
                    .clickable { onAction(AddAction.OnTxTypeChange(t)) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, color = if (state.txType == t) TrackerColors.Paper else TrackerColors.Ink)
            }
        }
    }

    // Currency picker
    if (state.currencies.isNotEmpty()) {
        CurrencyPicker(
            currencies = state.currencies,
            selected = state.selectedCurrencyCode,
            onSelect = { onAction(AddAction.OnCurrencyChange(it)) },
        )
    }

    val currencySymbol = state.currencies.find { it.code == state.selectedCurrencyCode }?.symbol ?: "$"
    TrackerTextField(
        value = state.amount,
        onValueChange = { onAction(AddAction.OnAmountChange(it)) },
        label = "Amount",
        leadingText = currencySymbol,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )

    TrackerTextField(
        value = state.merchant,
        onValueChange = { onAction(AddAction.OnMerchantChange(it)) },
        label = if (state.txType == TxType.INCOME) "From (payer / employer)" else "Merchant / description",
        modifier = Modifier.fillMaxWidth(),
    )

    // Category / Source chips
    val isIncome = state.txType == TxType.INCOME
    val items = if (isIncome) state.incomeSources else state.expenseCategories
    val selectedId = if (isIncome) state.selectedSourceId else state.selectedCategoryId
    val sectionLabel = if (isIncome) "Source" else "Category"

    if (items.isNotEmpty()) {
        Text(sectionLabel.uppercase(), fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = TrackerColors.Ink2.copy(alpha = 0.7f))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { cat ->
                val selected = cat.id == selectedId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) TrackerColors.Ink else TrackerColors.PaperWhite, RoundedCornerShape(999.dp))
                        .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                        .clickable {
                            if (isIncome) onAction(AddAction.OnSourceSelect(cat.id))
                            else onAction(AddAction.OnCategorySelect(cat.id))
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = TrackerIcons.fromKey(cat.icon), contentDescription = null, modifier = Modifier.size(14.dp), tint = if (selected) TrackerColors.Paper else TrackerColors.Ink)
                        Spacer(Modifier.width(6.dp))
                        Text(cat.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (selected) TrackerColors.Paper else TrackerColors.Ink)
                    }
                }
            }
        }
    }

    TrackerTextField(
        value = state.notes,
        onValueChange = { onAction(AddAction.OnNotesChange(it)) },
        label = "Notes (optional)",
        modifier = Modifier.fillMaxWidth(),
    )
}

// ── Goal sheet ────────────────────────────────────────────────────────────────

@Composable
private fun GoalSheetContent(state: AddState, onAction: (AddAction) -> Unit) {
    TrackerTextField(
        value = state.goalName,
        onValueChange = { onAction(AddAction.OnGoalNameChange(it)) },
        label = "Goal name",
        modifier = Modifier.fillMaxWidth(),
    )
    TrackerTextField(
        value = state.goalTarget,
        onValueChange = { onAction(AddAction.OnGoalTargetChange(it)) },
        label = "Target amount",
        leadingText = "$",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

// ── Debt sheet ────────────────────────────────────────────────────────────────

@Composable
private fun DebtSheetContent(state: AddState, onAction: (AddAction) -> Unit) {
    TrackerTextField(
        value = state.debtCreditor,
        onValueChange = { onAction(AddAction.OnDebtCreditorChange(it)) },
        label = "Creditor name",
        modifier = Modifier.fillMaxWidth(),
    )
    TrackerTextField(
        value = state.debtType,
        onValueChange = { onAction(AddAction.OnDebtTypeChange(it)) },
        label = "Type (LOAN, CARD, STUDENT)",
        modifier = Modifier.fillMaxWidth(),
    )
    TrackerTextField(
        value = state.debtBalance,
        onValueChange = { onAction(AddAction.OnDebtBalanceChange(it)) },
        label = "Current balance",
        leadingText = "$",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
    TrackerTextField(
        value = state.debtApr,
        onValueChange = { onAction(AddAction.OnDebtAprChange(it)) },
        label = "APR %",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
    TrackerTextField(
        value = state.debtMinPayment,
        onValueChange = { onAction(AddAction.OnDebtMinPayChange(it)) },
        label = "Min monthly payment",
        leadingText = "$",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

// ── Currency picker ───────────────────────────────────────────────────────────

@Composable
private fun CurrencyPicker(
    currencies: List<CurrencyUi>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column {
        Text("Currency".uppercase(), fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = TrackerColors.Ink2.copy(alpha = 0.7f))
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            currencies.forEach { currency ->
                val isSelected = currency.code == selected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isSelected) TrackerColors.Ink else TrackerColors.PaperWhite, RoundedCornerShape(999.dp))
                        .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                        .clickable { onSelect(currency.code) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(currency.symbol, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) TrackerColors.Paper else TrackerColors.Ink)
                        Spacer(Modifier.width(4.dp))
                        Text(currency.code, fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp, color = if (isSelected) TrackerColors.Paper.copy(alpha = 0.8f) else TrackerColors.Ink2)
                    }
                }
            }
        }
    }
}