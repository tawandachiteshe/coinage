package com.tawandachiteshe.coinage.feature.home

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tawandachiteshe.coinage.ui.components.PageHeader
import com.tawandachiteshe.coinage.ui.components.ProgressJar
import com.tawandachiteshe.coinage.ui.components.ReceiptRow
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.TrackerScaffold
import com.tawandachiteshe.coinage.ui.components.TrackerTab
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import com.tawandachiteshe.coinage.ui.theme.TrackerIcons

@Composable
fun HomeScreen(
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
    onManageJars: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val zoom = state.zoom

    val balanceWhole = state.balance.formatWhole()
    val balanceCents = state.balance.formatCents()
    val period = when (zoom) {
        Zoom.Year  -> "this year"
        Zoom.Month -> "this month"
        Zoom.Week  -> "this week"
    }

    TrackerScaffold(activeTab = TrackerTab.Home, onTabClick = onTabClick, onAddClick = onAddClick) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(TrackerColors.Mint, RoundedCornerShape(3.dp)))
                        Spacer(Modifier.width(6.dp))
                        Text("on track", fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = TrackerColors.Ink2.copy(alpha = 0.7f))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Hey, ${state.userName}.", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
                    Text("Small wins are still wins.", fontSize = 18.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Ink2)
                }
                // Avatar — taps to Profile
                val initial = state.userName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .popShadow(cornerRadius = 999.dp, offsetX = 2.dp, offsetY = 2.dp)
                        .clip(CircleShape)
                        .background(TrackerColors.Tangerine, CircleShape)
                        .border(1.8.dp, TrackerColors.Ink, CircleShape)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(initial, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Paper)
                }
            }

            // Balance card
            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.padding(horizontal = 22.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .popShadow(cornerRadius = 22.dp, offsetX = 4.dp, offsetY = 5.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(TrackerColors.Ink, RoundedCornerShape(22.dp))
                        .border(2.dp, TrackerColors.Ink, RoundedCornerShape(22.dp))
                        .padding(20.dp),
                ) {
                    Column {
                        Text(
                            text = "Balance · ${zoom.name.lowercase()}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.4.sp,
                            color = TrackerColors.Butter,
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(state.baseCurrencySymbol, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Paper.copy(alpha = 0.7f))
                            Text(balanceWhole, fontSize = 60.sp, fontWeight = FontWeight.Bold, lineHeight = 54.sp, color = TrackerColors.Paper)
                            Text(".$balanceCents", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Paper.copy(alpha = 0.7f))
                        }
                        Text(period, fontSize = 16.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Paper.copy(alpha = 0.78f))

                        // Period toggle
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(TrackerColors.Paper.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                                .border(1.dp, TrackerColors.Paper.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
                                .padding(3.dp),
                        ) {
                            listOf(Zoom.Year to "Y", Zoom.Month to "M", Zoom.Week to "W").forEach { (z, label) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (zoom == z) TrackerColors.Butter else Color.Transparent, RoundedCornerShape(999.dp))
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
                                        color = if (zoom == z) TrackerColors.Ink else TrackerColors.Paper,
                                    )
                                }
                            }
                        }

                        // Pinch hint bar
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(TrackerColors.Paper.copy(alpha = 0.25f)),
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("↤  pinch to zoom  ↦", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Paper.copy(alpha = 0.55f))
                            Text("+\$${state.income.formatWhole()} in · −\$${state.expenses.formatWhole()} out", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Paper.copy(alpha = 0.55f))
                        }
                    }
                }

                // Tape strip over card
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
                    Text("Your ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                    Text("jars", fontSize = 22.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Tangerine)
                }
                Text("MANAGE", fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = TrackerColors.Ink2.copy(alpha = 0.6f), modifier = Modifier.clickable { onManageJars() })
            }
            Spacer(Modifier.height(12.dp))

            // 3-column jars grid
            val jarRows = state.jars.chunked(3)
            if (state.jars.isEmpty() && !state.isLoading) {
                Text(
                    text = "No spending jars yet.",
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TrackerColors.Ink2.copy(alpha = 0.55f),
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
                            bgColor = TrackerColors.PaperWhite,
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
                                    Text(jar.name, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
                                    Text("\$${jar.spent.formatWhole()}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                    Text("of \$${jar.budgetLimit.formatWhole()}", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                    // Fill empty slots in last row
                    repeat(3 - rowJars.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Today's receipts
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Row {
                    Text("Recent ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                    Text("receipts", fontSize = 22.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Grape)
                }
                if (state.transactions.isNotEmpty()) {
                    Text("${state.transactions.size}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = TrackerColors.Ink2.copy(alpha = 0.6f))
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
                        color = TrackerColors.Ink2.copy(alpha = 0.55f),
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
                        icon = TrackerIcons.fromKey(tx.categoryIcon),
                        iconColor = if (tx.type == "INCOME") TrackerColors.Mint else TrackerColors.Tangerine,
                        tilt = tilts[i % tilts.size],
                        isIncome = tx.type == "INCOME",
                    )
                }
            }
    }
}

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
        TrackerColors.Grape
    }
}
