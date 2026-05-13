package com.tawandachiteshe.coinage.feature.goals

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
import androidx.compose.foundation.layout.offset
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
import com.tawandachiteshe.coinage.ui.components.PageHeader
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.StripedProgressBar
import com.tawandachiteshe.coinage.ui.components.TrackerTab
import com.tawandachiteshe.coinage.ui.components.TrackerTabBar
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.TrackerColors

private data class Goal(
    val title: String,
    val emoji: String,
    val saved: Int,
    val of: Int,
    val due: String,
    val pace: String,
    val color: Color,
    val tilt: Float,
)

@Composable
fun GoalsScreen(
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
) {
    val goals = remember {
        listOf(
            Goal("Iceland trip",     "❄️", 850,  2000, "Oct 2026", "+\$120/wk", TrackerColors.Sky,    -1.4f),
            Goal("Emergency fund",  "🛟",  1200, 3000, "no rush",  "+\$80/wk",  TrackerColors.Mint,    0.9f),
            Goal("New laptop",      "⌨️", 200,  1400, "Jan 2027", "+\$50/wk",  TrackerColors.Grape,  -0.6f),
            Goal("Mitski tickets",  "♪",   0,    120,  "Jul 30",   "just started", TrackerColors.Coral, 1.4f),
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
            PageHeader(
                eyebrow = "Goals · 4 in flight",
                title = "Saving",
                italicWord = "for…",
                accent = TrackerColors.Grape,
                kicker = "Each jar fills with every contribution. Tap a goal to nudge it forward.",
            )

            // Summary sticker
            Spacer(Modifier.height(14.dp))
            StickerCard(
                bgColor = TrackerColors.Butter,
                modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
                shadowX = 4.dp, shadowY = 5.dp, cornerRadius = 18.dp,
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .rotate(-6f)
                            .clip(RoundedCornerShape(28.dp))
                            .background(TrackerColors.Tangerine, RoundedCornerShape(28.dp))
                            .border(1.8.dp, TrackerColors.Ink, RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("37%", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TrackerColors.Ink)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("\$2,250 of \$6,520 saved", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                        Text("You're a steady saver. Keep showing up.", fontSize = 15.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Ink2)
                    }
                }
            }

            // Goal cards
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                goals.forEach { goal ->
                    val pct = (goal.saved.toFloat() / goal.of * 100f).coerceIn(0f, 100f)
                    StickerCard(
                        bgColor = TrackerColors.PaperWhite,
                        modifier = Modifier.fillMaxWidth(),
                        tilt = goal.tilt,
                        cornerRadius = 18.dp,
                        borderWidth = 1.8.dp,
                        shadowX = 4.dp,
                        shadowY = 4.dp,
                    ) {
                        Box {
                            // Tape strip
                            Box(
                                modifier = Modifier
                                    .padding(start = 24.dp)
                                    .size(52.dp, 16.dp)
                                    .rotate(-5f)
                                    .background(Color(0xB8FF8A4D))
                                    .align(Alignment.TopStart)
                                    .offset(y = (-8).dp),
                            )
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(goal.color, RoundedCornerShape(12.dp))
                                            .border(1.6.dp, TrackerColors.Ink, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) { Text(goal.emoji, fontSize = 22.sp) }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(goal.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                        Text("${goal.due} · ${goal.pace}", fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp, color = TrackerColors.Ink2.copy(alpha = 0.65f))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("\$${goal.saved.toLocaleString()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                        Text("/${(goal.of / 1000f).let { if (it == it.toInt().toFloat()) "${it.toInt()}k" else "${it}k" }}", fontSize = 12.sp, color = TrackerColors.Ink2.copy(alpha = 0.55f))
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                StripedProgressBar(pct = pct, color = goal.color, modifier = Modifier.fillMaxWidth())

                                Spacer(Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    listOf(10, 25, 50).forEach { amt ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(TrackerColors.Paper2, RoundedCornerShape(999.dp))
                                                .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                        ) {
                                            Text("+ \$$amt", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
                                        }
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Text("auto · fri", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.65f))
                                }
                            }
                        }
                    }
                }

                // Add new goal
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .border(2.dp, TrackerColors.Ink, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("+", fontSize = 22.sp, lineHeight = 22.sp, color = TrackerColors.Ink2)
                        Spacer(Modifier.width(10.dp))
                        Text("Plant a new goal", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink2)
                    }
                }
            }
        }

        TrackerTabBar(active = TrackerTab.Goals, onTabClick = onTabClick, onAddClick = onAddClick, modifier = Modifier.fillMaxSize())
    }
}

private fun Int.toLocaleString(): String {
    // Simple thousands formatting for KMP
    return if (this >= 1000) "${this / 1000},${(this % 1000).toString().padStart(3, '0')}" else this.toString()
}
