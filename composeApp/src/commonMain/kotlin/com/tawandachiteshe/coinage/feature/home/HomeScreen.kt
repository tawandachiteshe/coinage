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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tawandachiteshe.coinage.ui.components.PageHeader
import com.tawandachiteshe.coinage.ui.components.ProgressJar
import com.tawandachiteshe.coinage.ui.components.ReceiptRow
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.TrackerTab
import com.tawandachiteshe.coinage.ui.components.TrackerTabBar
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import com.tawandachiteshe.coinage.ui.theme.TrackerIcons

private enum class Zoom { Year, Month, Week }

private data class Jar(
    val label: String,
    val spent: Int,
    val of: Int,
    val color: Color,
    val icon: ImageVector,
    val tilt: Float,
)

@Composable
fun HomeScreen(
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
) {
    var zoom by remember { mutableStateOf(Zoom.Month) }

    val balance = when (zoom) {
        Zoom.Year  -> "24,180"
        Zoom.Month -> "2,847"
        Zoom.Week  -> "612"
    }
    val period = when (zoom) {
        Zoom.Year  -> "2026 so far"
        Zoom.Month -> "left this month"
        Zoom.Week  -> "left this week"
    }

    val jars = remember {
        listOf(
            Jar("Food",    340, 500,  TrackerColors.Tangerine, TrackerIcons.Utensils,     -1.5f),
            Jar("Rent",    1200, 1200, TrackerColors.Grape,   TrackerIcons.Home,          1.0f),
            Jar("Fun",     89,  200,  TrackerColors.Butter,   TrackerIcons.Music,        -0.8f),
            Jar("Subs",    42,  80,   TrackerColors.Sky,      TrackerIcons.Play,          1.5f),
            Jar("Saving",  250, 300,  TrackerColors.Mint,     TrackerIcons.PiggyBank,    -1.0f),
            Jar("Misc",    38,  100,  TrackerColors.Coral,    TrackerIcons.Gem,           0.8f),
        )
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
            // Header
            Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Tue · May 12",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.4.sp,
                        color = TrackerColors.Ink2.copy(alpha = 0.7f),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(TrackerColors.Mint, RoundedCornerShape(3.dp)))
                        Spacer(Modifier.width(6.dp))
                        Text("on track", fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = TrackerColors.Ink2.copy(alpha = 0.7f))
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("Hey, Maya.", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
                Text("Small wins are still wins.", fontSize = 18.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Ink2)
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
                            Text("$", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Paper.copy(alpha = 0.7f))
                            Text(balance, fontSize = 60.sp, fontWeight = FontWeight.Bold, lineHeight = 54.sp, color = TrackerColors.Paper)
                            Text(".50", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Paper.copy(alpha = 0.7f))
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
                                        .clickable { zoom = z }
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
                            Text("+\$3,420 in · −\$572 out", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Paper.copy(alpha = 0.55f))
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
                Text("TAP TO REFILL", fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = TrackerColors.Ink2.copy(alpha = 0.6f))
            }
            Spacer(Modifier.height(12.dp))

            // 3-column jars grid
            val rows = jars.chunked(3)
            rows.forEach { rowJars ->
                Row(
                    modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowJars.forEach { jar ->
                        val pct = (jar.spent.toFloat() / jar.of) * 100f
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
                                ProgressJar(pct = pct, color = jar.color, height = 56.dp, width = 22.dp)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(jar.label, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
                                    Text("\$${jar.spent}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                    Text("of \$${jar.of}", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.6f))
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
                    Text("Today's ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                    Text("receipts", fontSize = 22.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Grape)
                }
                Text("3 NEW", fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = TrackerColors.Ink2.copy(alpha = 0.6f))
            }
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ReceiptRow("Sunny Café", "6.40", "Food", "9:12am", TrackerIcons.Coffee, TrackerColors.Tangerine, tilt = -0.6f)
                ReceiptRow("Bandcamp · Nilüfer Y.", "9.99", "Fun", "11:30am", TrackerIcons.Music, TrackerColors.Butter, tilt = 0.8f)
                ReceiptRow("MTA · weekly pass", "34.00", "Transit", "yesterday", TrackerIcons.ArrowUpRight, TrackerColors.Sky, tilt = -0.4f)
            }
        }

        TrackerTabBar(
            active = TrackerTab.Home,
            onTabClick = onTabClick,
            onAddClick = onAddClick,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
