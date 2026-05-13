package com.tawandachiteshe.expensify.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.tawandachiteshe.expensify.ui.components.StickerCard
import com.tawandachiteshe.expensify.ui.components.TrackerTab
import com.tawandachiteshe.expensify.ui.components.TrackerTabBar
import com.tawandachiteshe.expensify.ui.components.popShadow
import com.tawandachiteshe.expensify.ui.theme.TrackerColors

private data class Category(val name: String, val value: Int, val color: Color)

@Composable
fun InsightsScreen(
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
) {
    val cats = remember {
        listOf(
            Category("Food",    540,  TrackerColors.Tangerine),
            Category("Rent",    1200, TrackerColors.Grape),
            Category("Fun",     189,  TrackerColors.Butter),
            Category("Transit", 92,   TrackerColors.Sky),
            Category("Misc",    38,   TrackerColors.Coral),
        )
    }
    val total = cats.sumOf { it.value }

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
            // Header
            Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                Text("April · in review".uppercase(), fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.4.sp, color = TrackerColors.Ink2.copy(alpha = 0.7f))
                Spacer(Modifier.height(4.dp))
                Row {
                    Text("A pretty ", fontSize = 36.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp, color = TrackerColors.Ink)
                    Text("good month.", fontSize = 36.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, lineHeight = 34.sp, color = TrackerColors.Tangerine)
                }
                Spacer(Modifier.height(6.dp))
                Text("You spent \$134 less than March. Kept three weeks under budget. The receipts tell a story.", fontSize = 13.5.sp, lineHeight = 19.sp, color = TrackerColors.Ink2)
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
                            Text(total.toLocaleString(), fontSize = 54.sp, fontWeight = FontWeight.Bold, lineHeight = 50.sp, color = TrackerColors.Ink)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Tag("↓ \$134 vs March", TrackerColors.Ink, TrackerColors.Butter)
                            Tag("3-wk streak 🔥", TrackerColors.Ink, TrackerColors.Paper)
                        }
                    }
                    // Star decoration
                    Text("★", fontSize = 36.sp, color = TrackerColors.Butter, modifier = Modifier.align(Alignment.TopEnd).rotate(10f))
                }
            }

            // Stacked bar chart
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text("Where it went", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                    Text("5 categories".uppercase(), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = TrackerColors.Ink2.copy(alpha = 0.6f))
                }
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
                        Box(
                            modifier = Modifier
                                .weight(cat.value.toFloat())
                                .height(28.dp)
                                .background(cat.color)
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
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(cat.color).border(1.2.dp, TrackerColors.Ink, RoundedCornerShape(3.dp)))
                                    Spacer(Modifier.width(8.dp))
                                    Text(cat.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink, modifier = Modifier.weight(1f))
                                    Text("\$${cat.value}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
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
                    Text("✦", fontSize = 22.sp, color = TrackerColors.Butter)
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

        TrackerTabBar(active = TrackerTab.Insights, onTabClick = onTabClick, onAddClick = onAddClick, modifier = Modifier.fillMaxSize())
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

private fun Int.toLocaleString(): String =
    if (this >= 1000) "${this / 1000},${(this % 1000).toString().padStart(3, '0')}" else this.toString()
