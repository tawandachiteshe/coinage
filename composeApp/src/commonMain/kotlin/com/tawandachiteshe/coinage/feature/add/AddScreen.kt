package com.tawandachiteshe.coinage.feature.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tawandachiteshe.coinage.ui.components.PageHeader
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.TrackerTab
import com.tawandachiteshe.coinage.ui.components.TrackerTabBar
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import com.tawandachiteshe.coinage.ui.theme.TrackerIcons
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddScreen(
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
    onSaved: () -> Unit = {},
    viewModel: AddViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddEvent.Saved -> onSaved()
                is AddEvent.ShowError -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrackerColors.Paper),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 56.dp, bottom = 120.dp),
        ) {
            val (title, italicWord) = when (state.addType) {
                AddType.Transaction -> "New" to "transaction."
                AddType.Goal        -> "New" to "goal."
                AddType.Debt        -> "New" to "debt."
            }
            PageHeader(
                title = title,
                italicWord = italicWord,
                eyebrow = "add · record",
                accent = TrackerColors.Tangerine,
                kicker = when (state.addType) {
                    AddType.Transaction -> "Every receipt tells a story. Log it."
                    AddType.Goal        -> "Plant the seed. Watch it grow."
                    AddType.Debt        -> "Name it to tame it."
                },
            )

            // Type picker
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 22.dp)
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
                            .background(if (state.addType == type) TrackerColors.Ink else Color.Transparent, RoundedCornerShape(999.dp))
                            .clickable { viewModel.onAction(AddAction.OnAddTypeChange(type)) }
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, color = if (state.addType == type) TrackerColors.Paper else TrackerColors.Ink)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Form body
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                when (state.addType) {
                    AddType.Transaction -> {
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
                                        .clickable { viewModel.onAction(AddAction.OnTxTypeChange(t)) }
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
                                onSelect = { viewModel.onAction(AddAction.OnCurrencyChange(it)) },
                            )
                        }

                        // Amount
                        val currencySymbol = state.currencies.find { it.code == state.selectedCurrencyCode }?.symbol ?: "$"
                        OutlinedTextField(
                            value = state.amount,
                            onValueChange = { viewModel.onAction(AddAction.OnAmountChange(it)) },
                            label = { Text("Amount") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = { Text(currencySymbol, fontSize = 16.sp, color = TrackerColors.Ink2) },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        OutlinedTextField(
                            value = state.merchant,
                            onValueChange = { viewModel.onAction(AddAction.OnMerchantChange(it)) },
                            label = { Text(if (state.txType == TxType.INCOME) "From (payer / employer)" else "Merchant / description") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Source or Category chips
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
                                                if (isIncome) viewModel.onAction(AddAction.OnSourceSelect(cat.id))
                                                else viewModel.onAction(AddAction.OnCategorySelect(cat.id))
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

                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = { viewModel.onAction(AddAction.OnNotesChange(it)) },
                            label = { Text("Notes (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    AddType.Goal -> {
                        OutlinedTextField(
                            value = state.goalName,
                            onValueChange = { viewModel.onAction(AddAction.OnGoalNameChange(it)) },
                            label = { Text("Goal name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = state.goalTarget,
                            onValueChange = { viewModel.onAction(AddAction.OnGoalTargetChange(it)) },
                            label = { Text("Target amount") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = { Text("$", fontSize = 16.sp, color = TrackerColors.Ink2) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    AddType.Debt -> {
                        OutlinedTextField(
                            value = state.debtCreditor,
                            onValueChange = { viewModel.onAction(AddAction.OnDebtCreditorChange(it)) },
                            label = { Text("Creditor name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = state.debtType,
                            onValueChange = { viewModel.onAction(AddAction.OnDebtTypeChange(it)) },
                            label = { Text("Type (LOAN, CARD, STUDENT…)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = state.debtBalance,
                            onValueChange = { viewModel.onAction(AddAction.OnDebtBalanceChange(it)) },
                            label = { Text("Current balance") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = { Text("$", fontSize = 16.sp, color = TrackerColors.Ink2) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = state.debtApr,
                            onValueChange = { viewModel.onAction(AddAction.OnDebtAprChange(it)) },
                            label = { Text("APR %") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = state.debtMinPayment,
                            onValueChange = { viewModel.onAction(AddAction.OnDebtMinPayChange(it)) },
                            label = { Text("Min monthly payment") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = { Text("$", fontSize = 16.sp, color = TrackerColors.Ink2) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                // Error
                if (state.error != null) {
                    Text(state.error!!, fontSize = 13.sp, color = TrackerColors.Cherry, fontFamily = FontFamily.Monospace)
                }

                // Save button
                Spacer(Modifier.height(4.dp))
                StickerCard(bgColor = TrackerColors.Tangerine, modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp, borderWidth = 2.dp, shadowX = 4.dp, shadowY = 4.dp) {
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

        TrackerTabBar(active = null, onTabClick = onTabClick, onAddClick = onAddClick, modifier = Modifier.fillMaxSize())
    }
}

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