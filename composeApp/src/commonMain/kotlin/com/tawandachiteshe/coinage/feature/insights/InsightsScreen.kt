package com.tawandachiteshe.coinage.feature.insights

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tawandachiteshe.coinage.feature.home.Zoom
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.TrackerScaffold
import com.tawandachiteshe.coinage.ui.components.TrackerTab
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import com.tawandachiteshe.coinage.ui.theme.TrackerIcons
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InsightsScreen(
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
    viewModel: InsightsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cats = state.categoryTotals
    val total = state.totalSpent

    TrackerScaffold(activeTab = TrackerTab.Insights, onTabClick = onTabClick, onAddClick = onAddClick) {
            // Header
            Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                val periodLabel = when (state.zoom) {
                    Zoom.Week  -> "This week · in review"
                    Zoom.Month -> "This month · in review"
                    Zoom.Year  -> "This year · in review"
                }
                Text(periodLabel.uppercase(), fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.4.sp, color = TrackerColors.Ink2.copy(alpha = 0.7f))
                Spacer(Modifier.height(4.dp))
                Row {
                    Text("Where did it ", fontSize = 36.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp, color = TrackerColors.Ink)
                    Text("all go?", fontSize = 36.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, lineHeight = 34.sp, color = TrackerColors.Tangerine)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (cats.isEmpty()) "No transactions yet for this period."
                    else "${cats.size} categor${if (cats.size == 1) "y" else "ies"} · the receipts tell a story.",
                    fontSize = 13.5.sp, lineHeight = 19.sp, color = TrackerColors.Ink2,
                )
            }

            // Big spend number
            Spacer(Modifier.height(18.dp))
            StickerCard(
                bgColor = TrackerColors.Tangerine,
                modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
                cornerRadius = 22.dp, borderWidth = 2.dp, shadowX = 4.dp, shadowY = 5.dp,
            ) {
                Box(modifier = Modifier.padding(18.dp)) {
                    Column {
                        Text("Total spent".uppercase(), fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.2.sp, color = TrackerColors.Ink.copy(alpha = 0.8f))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink.copy(alpha = 0.85f))
                            Text(total.fmtWhole(), fontSize = 54.sp, fontWeight = FontWeight.Bold, lineHeight = 50.sp, color = TrackerColors.Ink)
                        }
                        Spacer(Modifier.height(8.dp))
                        // Zoom toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(TrackerColors.Ink.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                                .border(1.dp, TrackerColors.Ink.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
                                .padding(3.dp),
                        ) {
                            listOf(Zoom.Year to "Y", Zoom.Month to "M", Zoom.Week to "W").forEach { (z, label) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (state.zoom == z) TrackerColors.Ink else Color.Transparent, RoundedCornerShape(999.dp))
                                        .clickable { viewModel.onAction(InsightsAction.OnZoomChange(z)) }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(label, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = if (state.zoom == z) TrackerColors.Paper else TrackerColors.Ink)
                                }
                            }
                        }
                    }
                    // Star decoration
                    Icon(TrackerIcons.Star, contentDescription = null, modifier = Modifier.size(36.dp).align(Alignment.TopEnd).rotate(10f), tint = TrackerColors.Butter)
                }
            }

            // Stacked bar chart
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text("Where it went", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                    Text("${cats.size} categor${if (cats.size == 1) "y" else "ies"}".uppercase(), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = TrackerColors.Ink2.copy(alpha = 0.6f))
                }
                if (cats.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .popShadow(cornerRadius = 999.dp, offsetX = 2.dp, offsetY = 2.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .border(1.6.dp, TrackerColors.Ink, RoundedCornerShape(999.dp)),
                    ) {
                        cats.forEachIndexed { i, cat ->
                            val catColor = parseHexColor(cat.colorHex)
                            Box(
                                modifier = Modifier
                                    .weight(cat.total.toFloat().coerceAtLeast(0.01f))
                                    .height(28.dp)
                                    .background(catColor)
                                    .then(
                                        if (i < cats.lastIndex) Modifier.border(width = 0.dp, color = Color.Transparent).padding(end = 1.dp)
                                        else Modifier
                                    ),
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    // Legend
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        cats.chunked(2).forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                row.forEach { cat ->
                                    val catColor = parseHexColor(cat.colorHex)
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(catColor).border(1.2.dp, TrackerColors.Ink, RoundedCornerShape(3.dp)))
                                        Spacer(Modifier.width(8.dp))
                                        Text(cat.categoryName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink, modifier = Modifier.weight(1f))
                                        Text("\$${cat.total.fmtWhole()}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.7f))
                                    }
                                }
                                if (row.size < 2) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                } else if (!state.isLoading) {
                    Spacer(Modifier.height(8.dp))
                    Text("No spending data yet for this period.", fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.55f))
                }
            }

            // Moments collage
            Spacer(Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                Row {
                    Text("Moments ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                    Text("worth a sticker", fontSize = 22.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Grape)
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MomentCard(
                        eyebrow = "BIGGEST SPLURGE",
                        title = "Thai dinner with Theo",
                        value = "\$48.20",
                        bgColor = TrackerColors.Butter,
                        tilt = -1.6f,
                        modifier = Modifier.weight(1f),
                    )
                    MomentCard(
                        eyebrow = "SAVED THIS MONTH",
                        title = "+\$48 vs last month",
                        value = "\$312",
                        bgColor = TrackerColors.Mint,
                        tilt = 1.4f,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(12.dp))
                // 3-week streak card — full width
                StickerCard(
                    bgColor = TrackerColors.Sky,
                    modifier = Modifier.fillMaxWidth().rotate(0.8f),
                    cornerRadius = 16.dp,
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(50.dp).rotate(-8f)
                                .clip(RoundedCornerShape(25.dp))
                                .background(TrackerColors.Paper)
                                .border(1.6.dp, TrackerColors.Ink, RoundedCornerShape(25.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("3w", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TrackerColors.Ink)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Three weeks under budget", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                            Text("Quietly impressive. One more and you'll match your record.", fontSize = 14.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Ink2)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MomentCard(
                        eyebrow = "MOST-VISITED",
                        title = "Sunny Café",
                        value = "14 visits · \$89.60",
                        isValueSmall = true,
                        bgColor = TrackerColors.Coral,
                        tilt = -0.8f,
                        modifier = Modifier.weight(1f),
                    )
                    MomentCard(
                        eyebrow = "QUIETEST DAY",
                        title = "Sunday Apr 14",
                        value = "\$0",
                        bgColor = TrackerColors.Paper,
                        tilt = 1.0f,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Footer mantra
                Spacer(Modifier.height(22.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .popShadow(cornerRadius = 14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(TrackerColors.Ink)
                        .border(1.8.dp, TrackerColors.Ink, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(TrackerIcons.Star, contentDescription = null, modifier = Modifier.size(20.dp), tint = TrackerColors.Butter)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "\"Spending less, hugging tighter.\" — May's mantra, picked just for you.",
                        fontSize = 15.sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 20.sp,
                        color = TrackerColors.Paper,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
    }
}

@Composable
private fun MomentCard(
    eyebrow: String,
    title: String,
    value: String,
    bgColor: Color,
    tilt: Float,
    modifier: Modifier = Modifier,
    isValueSmall: Boolean = false,
) {
    StickerCard(bgColor = bgColor, modifier = modifier.rotate(tilt), cornerRadius = 16.dp) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(eyebrow, fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.2.sp, color = TrackerColors.Ink2.copy(alpha = 0.7f))
            Spacer(Modifier.height(4.dp))
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink, lineHeight = 18.sp)
            Text(value, fontSize = if (isValueSmall) 11.sp else 22.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink, fontFamily = if (isValueSmall) FontFamily.Monospace else FontFamily.Default)
        }
    }
}

@Composable
private fun Tag(label: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 3.dp),
    ) {
        Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
private fun IconTag(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textColor: Color,
    bgColor: Color,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = textColor)
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = textColor)
    }
}

private fun Double.fmtWhole(): String {
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
