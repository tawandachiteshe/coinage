package com.tawandachiteshe.coinage.feature.debt

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
import com.tawandachiteshe.coinage.ui.components.PageHeader
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.TrackerTab
import com.tawandachiteshe.coinage.ui.components.TrackerTabBar
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.TrackerColors

private data class Debt(
    val who: String,
    val kind: String,
    val orig: Int,
    val left: Int,
    val apr: String,
    val next: String,
    val color: Color,
    val tilt: Float,
    val almostDone: Boolean = false,
)

@Composable
fun DebtScreen(
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
) {
    val debts = remember {
        listOf(
            Debt("Chase Sapphire",   "Credit card",         3000,  1240, "21.4% APR",           "\$240 · May 28",  TrackerColors.Cherry, -0.8f),
            Debt("Sallie Mae",       "Student loan",       18000, 14200, "5.5% APR",             "\$280 · Jun 1",   TrackerColors.Grape,   0.6f),
            Debt("Sarah (friend)",   "IOU · brunch fund",    200,    40, "no interest, just love","\$40 · whenever", TrackerColors.Coral,  -1.2f, true),
        )
    }
    val totalLeft = debts.sumOf { it.left }
    val totalOrig = debts.sumOf { it.orig }
    val paidPct   = ((totalOrig - totalLeft).toFloat() / totalOrig * 100f).toInt()

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
                        Text(totalLeft.toLocaleString(), fontSize = 48.sp, fontWeight = FontWeight.Bold, lineHeight = 46.sp, color = TrackerColors.Paper)
                    }
                    Text("down from \$${totalOrig.toLocaleString()} · keep going.", fontSize = 15.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Paper.copy(alpha = 0.75f))

                    // Mountain SVG — drawn as a simple layered mountain
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
                        Text("\$${totalOrig.toLocaleString()}", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Paper.copy(alpha = 0.55f))
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
                    ) { Text("❄", fontSize = 18.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Snowball plan · pay smallest first", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                        Text("Sarah → Chase → Sallie · debt-free aug 2029", fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp, color = TrackerColors.Ink2.copy(alpha = 0.65f))
                    }
                    Text("→", fontSize = 18.sp, color = TrackerColors.Ink)
                }
            }

            // Debt cards
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                debts.forEach { debt ->
                    val debtPct = ((debt.orig - debt.left).toFloat() / debt.orig * 100f).toInt()
                    StickerCard(
                        bgColor = TrackerColors.PaperWhite,
                        modifier = Modifier.fillMaxWidth(),
                        tilt = debt.tilt,
                        cornerRadius = 16.dp,
                        borderWidth = 1.8.dp,
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            if (debt.almostDone) {
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
                                Box(modifier = Modifier.width(8.dp).height(120.dp).clip(RoundedCornerShape(4.dp)).background(debt.color).border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(4.dp)))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(debt.who, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                    Text("${debt.kind} · ${debt.apr}", fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp, color = TrackerColors.Ink2.copy(alpha = 0.65f))
                                    Spacer(Modifier.height(10.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("\$${debt.left.toLocaleString()}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                        Spacer(Modifier.width(8.dp))
                                        Text("of \$${debt.orig.toLocaleString()} left", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.55f))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(10.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(TrackerColors.Paper2)
                                            .border(1.3.dp, TrackerColors.Ink, RoundedCornerShape(999.dp)),
                                    ) {
                                        Box(Modifier.fillMaxWidth(debtPct / 100f).height(10.dp).background(debt.color))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row {
                                            Text("next: ", fontSize = 13.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Ink2)
                                            Text(debt.next, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(TrackerColors.Butter)
                                                .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
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
            }
        }

        TrackerTabBar(active = TrackerTab.Debt, onTabClick = onTabClick, onAddClick = onAddClick, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun MountainVisual(paidPct: Int, modifier: Modifier = Modifier) {
    // Simple mountain using stacked boxes to approximate the SVG
    Box(modifier = modifier) {
        Text(
            text = "⛰️  $paidPct% of the mountain climbed",
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
            fontFamily = FontFamily.Serif,
            color = TrackerColors.Butter,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

private fun Int.toLocaleString(): String =
    if (this >= 1000) "${this / 1000},${(this % 1000).toString().padStart(3, '0')}" else this.toString()
