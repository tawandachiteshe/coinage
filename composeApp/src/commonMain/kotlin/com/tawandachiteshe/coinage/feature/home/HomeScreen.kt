package com.tawandachiteshe.coinage.feature.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tawandachiteshe.coinage.feature.debt.DebtState
import com.tawandachiteshe.coinage.feature.debt.DebtViewModel
import com.tawandachiteshe.coinage.feature.goals.GoalsState
import com.tawandachiteshe.coinage.feature.goals.GoalsViewModel
import com.tawandachiteshe.coinage.ui.components.PageHeader
import com.tawandachiteshe.coinage.ui.components.ProgressJar
import com.tawandachiteshe.coinage.ui.components.ReceiptRow
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.CoinageScaffold
import com.tawandachiteshe.coinage.ui.components.CoinageTab
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.CoinageColors
import com.tawandachiteshe.coinage.ui.theme.CoinageIcons

@Composable
fun HomeScreen(
    onTabClick: (CoinageTab) -> Unit,
    onAddClick: () -> Unit,
    onManageJars: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val zoom = state.zoom

    val debtVm: DebtViewModel = koinViewModel()
    val goalVm: GoalsViewModel = koinViewModel()
    val debtState by debtVm.state.collectAsStateWithLifecycle()
    val goalState by goalVm.state.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { 3 })

    val period = when (zoom) {
        Zoom.Year  -> "this year"
        Zoom.Month -> "this month"
        Zoom.Week  -> "this week"
    }

    CoinageScaffold(activeTab = CoinageTab.Home, onTabClick = onTabClick, onAddClick = onAddClick) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(CoinageColors.Mint, RoundedCornerShape(3.dp)))
                    Spacer(Modifier.width(6.dp))
                    Text("on track", fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = CoinageColors.Ink2.copy(alpha = 0.7f))
                }
                Spacer(Modifier.height(6.dp))
                Text("Hey, ${state.userName}.", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = CoinageColors.Ink)
                Text("Small wins are still wins.", fontSize = 18.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = CoinageColors.Ink2)
            }
            val initial = state.userName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .popShadow(cornerRadius = 999.dp, offsetX = 2.dp, offsetY = 2.dp)
                    .clip(CircleShape)
                    .background(CoinageColors.Tangerine, CircleShape)
                    .border(1.8.dp, CoinageColors.Ink, CircleShape)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(initial, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CoinageColors.Paper)
            }
        }

        // Swipeable balance card
        Spacer(Modifier.height(20.dp))
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .popShadow(cornerRadius = 22.dp, offsetX = 4.dp, offsetY = 5.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(CoinageColors.Ink, RoundedCornerShape(22.dp))
                    .border(2.dp, CoinageColors.Ink, RoundedCornerShape(22.dp)),
            ) {
                Column {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().height(216.dp),
                    ) { page ->
                        when (page) {
                            0 -> BalancePage(state, zoom, period, viewModel)
                            1 -> CashflowPage(state)
                            else -> DebtsGoalsPage(debtState, goalState)
                        }
                    }
                    // Page indicator dots
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(3) { i ->
                            val active = pagerState.currentPage == i
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .then(if (active) Modifier.width(16.dp).height(5.dp) else Modifier.size(5.dp))
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        if (active) CoinageColors.Butter
                                        else CoinageColors.Paper.copy(alpha = 0.25f)
                                    ),
                            )
                        }
                    }
                }
            }
            // Tape strip decoration
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp)
                    .offset(y = (-8).dp)
                    .size(56.dp, 16.dp)
                    .rotate(-7f)
                    .background(Color(0xB3FF8A4D)),
            )
        }

        // Jars section
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Row {
                Text("Your ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CoinageColors.Ink)
                Text("jars", fontSize = 22.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = CoinageColors.Tangerine)
            }
            Text("MANAGE", fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = CoinageColors.Ink2.copy(alpha = 0.6f), modifier = Modifier.clickable { onManageJars() })
        }
        Spacer(Modifier.height(12.dp))

        val jarRows = state.jars.chunked(3)
        if (state.jars.isEmpty() && !state.isLoading) {
            Text(
                text = "No spending jars yet.",
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = CoinageColors.Ink2.copy(alpha = 0.55f),
            )
        }
        jarRows.forEach { rowJars ->
            Row(
                modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowJars.forEach { jar ->
                    val jarColor = parseHexColor(jar.colorHex)
                    val pct = if (jar.budgetLimit > 0) (jar.spent / jar.budgetLimit * 100f).toFloat().coerceIn(0f, 100f) else 0f
                    StickerCard(
                        bgColor = CoinageColors.PaperWhite,
                        modifier = Modifier.weight(1f),
                        tilt = jar.tilt,
                        cornerRadius = 14.dp,
                        borderWidth = 1.6.dp,
                        shadowX = 2.5.dp,
                        shadowY = 3.dp,
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            ProgressJar(pct = pct, color = jarColor, height = 56.dp, width = 22.dp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(jar.name, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = CoinageColors.Ink)
                                Text("\$${jar.spent.formatWhole()}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = CoinageColors.Ink)
                                Text("of \$${jar.budgetLimit.formatWhole()}", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = CoinageColors.Ink2.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
                repeat(3 - rowJars.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(10.dp))
        }

        // Recent receipts
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Row {
                Text("Recent ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CoinageColors.Ink)
                Text("receipts", fontSize = 22.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = CoinageColors.Grape)
            }
            if (state.transactions.isNotEmpty()) {
                Text("${state.transactions.size}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = CoinageColors.Ink2.copy(alpha = 0.6f))
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.transactions.isEmpty() && !state.isLoading) {
                Text(
                    text = "No transactions yet — tap + to add one.",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CoinageColors.Ink2.copy(alpha = 0.55f),
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            val tilts = listOf(-0.6f, 0.8f, -0.4f, 0.5f, -0.3f)
            state.transactions.take(5).forEachIndexed { i, tx ->
                ReceiptRow(
                    merchant = tx.merchant,
                    amount = tx.amount.formatWhole(),
                    category = tx.categoryName,
                    time = if (tx.type == "INCOME") "income" else "expense",
                    icon = CoinageIcons.fromKey(tx.categoryIcon),
                    iconColor = if (tx.type == "INCOME") CoinageColors.Mint else CoinageColors.Tangerine,
                    tilt = tilts[i % tilts.size],
                    isIncome = tx.type == "INCOME",
                )
            }
        }
    }
}

// ── Card pages ─────────────────────────────────────────────────────────────

@Composable
private fun BalancePage(
    state: HomeState,
    zoom: Zoom,
    period: String,
    viewModel: HomeViewModel,
) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp)) {
        Text(
            text = "Balance · ${zoom.name.lowercase()}",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.4.sp,
            color = CoinageColors.Butter,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(state.baseCurrencySymbol, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CoinageColors.Paper.copy(alpha = 0.7f))
            Text(state.balance.formatWhole(), fontSize = 54.sp, fontWeight = FontWeight.Bold, lineHeight = 48.sp, color = CoinageColors.Paper)
            Text(".${state.balance.formatCents()}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CoinageColors.Paper.copy(alpha = 0.7f))
        }
        Text(period, fontSize = 15.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = CoinageColors.Paper.copy(alpha = 0.78f))
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(CoinageColors.Paper.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                .border(1.dp, CoinageColors.Paper.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
                .padding(3.dp),
        ) {
            listOf(Zoom.Year to "Y", Zoom.Month to "M", Zoom.Week to "W").forEach { (z, label) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (zoom == z) CoinageColors.Butter else Color.Transparent, RoundedCornerShape(999.dp))
                        .clickable { viewModel.onAction(HomeAction.OnZoomChange(z)) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = if (zoom == z) CoinageColors.Ink else CoinageColors.Paper,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CoinageColors.Paper.copy(alpha = 0.2f)))
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("swipe for more  →", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = CoinageColors.Paper.copy(alpha = 0.45f))
            Text("+\$${state.income.formatWhole()} in · −\$${state.expenses.formatWhole()} out", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = CoinageColors.Paper.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun CashflowPage(state: HomeState) {
    val total = state.income + state.expenses
    val incomeF = if (total > 0) (state.income / total).toFloat() else 0f
    val expenseF = 1f - incomeF
    val net = state.income - state.expenses

    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp)) {
        Text(
            text = "Cashflow · ${state.zoom.name.lowercase()}",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.4.sp,
            color = CoinageColors.Butter,
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Donut chart
            Box(modifier = Modifier.size(108.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(108.dp)) {
                    val strokeWidth = 15.dp.toPx()
                    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    drawCircle(color = Color.White.copy(alpha = 0.08f), style = stroke)
                    if (total > 0) {
                        drawArc(
                            color = CoinageColors.Mint,
                            startAngle = -90f,
                            sweepAngle = 360f * incomeF,
                            useCenter = false,
                            style = stroke,
                        )
                        if (expenseF > 0f) {
                            drawArc(
                                color = CoinageColors.Coral,
                                startAngle = -90f + 360f * incomeF,
                                sweepAngle = 360f * expenseF,
                                useCenter = false,
                                style = stroke,
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (total > 0) "${(incomeF * 100).toInt()}%" else "—",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoinageColors.Paper,
                    )
                    Text(
                        text = if (total > 0) "saved" else "no data",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CoinageColors.Paper.copy(alpha = 0.5f),
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CardStat(dot = CoinageColors.Mint,   label = "income",   value = "+\$${state.income.formatWhole()}")
                CardStat(dot = CoinageColors.Coral,  label = "expenses",  value = "−\$${state.expenses.formatWhole()}")
                CardStat(
                    dot   = if (net >= 0) CoinageColors.Mint else CoinageColors.Cherry,
                    label = "net",
                    value = "${if (net >= 0) "+" else "−"}\$${kotlin.math.abs(net).formatWhole()}",
                    valueColor = if (net >= 0) CoinageColors.Mint else CoinageColors.Cherry,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CoinageColors.Paper.copy(alpha = 0.2f)))
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("← swipe  →", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = CoinageColors.Paper.copy(alpha = 0.45f))
            Text("${state.transactions.size} transactions", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = CoinageColors.Paper.copy(alpha = 0.45f))
        }
    }
}

@Composable
private fun DebtsGoalsPage(debtState: DebtState, goalState: GoalsState) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp)) {
        Text(
            text = "On deck",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.4.sp,
            color = CoinageColors.Butter,
        )
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            // Debts
            Column(modifier = Modifier.weight(1f)) {
                Text("total owed", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = CoinageColors.Paper.copy(alpha = 0.5f), letterSpacing = 0.8.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (debtState.totalOwed > 0) "−\$${debtState.totalOwed.formatWhole()}" else "debt free",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (debtState.totalOwed > 0) CoinageColors.Coral else CoinageColors.Mint,
                )
                if (debtState.debts.isNotEmpty()) {
                    Text("${debtState.debts.size} account${if (debtState.debts.size != 1) "s" else ""}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = CoinageColors.Paper.copy(alpha = 0.4f))
                }
            }
            // Goals
            Column(modifier = Modifier.weight(1f)) {
                Text("goals saved", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = CoinageColors.Paper.copy(alpha = 0.5f), letterSpacing = 0.8.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (goalState.totalTarget > 0) "+\$${goalState.totalSaved.formatWhole()}" else "no goals",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoinageColors.Mint,
                )
                if (goalState.totalTarget > 0) {
                    Text("of \$${goalState.totalTarget.formatWhole()}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = CoinageColors.Paper.copy(alpha = 0.4f))
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        // Top 2 goals with mini progress bars
        if (goalState.goals.isNotEmpty()) {
            goalState.goals.take(2).forEach { goal ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(goal.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CoinageColors.Paper, modifier = Modifier.weight(1f))
                    Text("${goal.pct.toInt()}%", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = CoinageColors.Butter)
                }
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(CoinageColors.Paper.copy(alpha = 0.12f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((goal.pct / 100f).coerceIn(0f, 1f))
                            .height(4.dp)
                            .background(CoinageColors.Mint, RoundedCornerShape(999.dp)),
                    )
                }
                Spacer(Modifier.height(5.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CoinageColors.Paper.copy(alpha = 0.2f)))
        Spacer(Modifier.height(10.dp))
        Text("←  swipe back", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = CoinageColors.Paper.copy(alpha = 0.45f))
    }
}

@Composable
private fun CardStat(
    dot: Color,
    label: String,
    value: String,
    valueColor: Color = CoinageColors.Paper,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(dot))
        Spacer(Modifier.width(6.dp))
        Column {
            Text(label, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = CoinageColors.Paper.copy(alpha = 0.5f), letterSpacing = 0.5.sp)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

private fun Double.formatWhole(): String {
    val l = kotlin.math.abs(toLong())
    return if (l >= 1000) "${l / 1000},${(l % 1000).toString().padStart(3, '0')}" else l.toString()
}

private fun Double.formatCents(): String =
    ((kotlin.math.abs(this) % 1.0) * 100).toInt().toString().padStart(2, '0')

private fun parseHexColor(hex: String): Color {
    val clean = hex.trimStart('#').padStart(6, '0')
    return try {
        Color(
            red   = clean.substring(0, 2).toInt(16) / 255f,
            green = clean.substring(2, 4).toInt(16) / 255f,
            blue  = clean.substring(4, 6).toInt(16) / 255f,
        )
    } catch (_: Exception) {
        CoinageColors.Grape
    }
}