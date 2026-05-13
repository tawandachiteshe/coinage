package com.tawandachiteshe.expensify.feature.add

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tawandachiteshe.expensify.ui.components.PageHeader
import com.tawandachiteshe.expensify.ui.components.StickerCard
import com.tawandachiteshe.expensify.ui.components.TrackerTab
import com.tawandachiteshe.expensify.ui.components.TrackerTabBar
import com.tawandachiteshe.expensify.ui.components.popShadow
import com.tawandachiteshe.expensify.ui.theme.TrackerColors
import kotlin.math.abs
import kotlin.math.roundToInt

private data class QueueCard(
    val merchant: String,
    val amount: String,
    val timestamp: String,
    val hint: String,
    val glyph: String,
)

private data class CatTarget(
    val label: String,
    val color: Color,
    val dir: Char, // 'l','r','u','d'
)

@Composable
fun AddScreen(
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
) {
    val queue = remember {
        listOf(
            QueueCard("Trader Joe's",  "42.18", "today · 6:14pm",   "Groceries · debit ··3421", "🛒"),
            QueueCard("Apple Music",   "10.99", "today · 12:00pm",  "Recurring · monthly",       "♪"),
            QueueCard("Hara Sushi",    "28.50", "yesterday",        "Lunch · venmo",             "🍣"),
        )
    }
    val cats = remember {
        listOf(
            CatTarget("Need",  TrackerColors.Mint,  'l'),
            CatTarget("Want",  TrackerColors.Coral, 'r'),
            CatTarget("Save",  TrackerColors.Mint,  'u'),
            CatTarget("Skip",  TrackerColors.Paper3,'d'),
        )
    }

    var idx by remember { mutableStateOf(0) }
    var dragX by remember { mutableStateOf(0f) }
    var dragY by remember { mutableStateOf(0f) }

    val card = queue[idx % queue.size]

    val threshold = 200f
    val intent: Char? = if (abs(dragX) > abs(dragY)) {
        when {
            dragX > 80f  -> 'r'
            dragX < -80f -> 'l'
            else -> null
        }
    } else {
        when {
            dragY < -80f -> 'u'
            dragY > 80f  -> 'd'
            else -> null
        }
    }

    val cardOffsetX = dragX
    val cardOffsetY = dragY

    val cardRotation = (dragX * 0.04f).coerceIn(-20f, 20f)
    val stampAlpha = (abs(dragX) + abs(dragY)).coerceIn(0f, 200f) / 200f

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
                title = "What kind of",
                italicWord = "spend?",
                eyebrow = "${(queue.size - idx % queue.size)} to file",
                accent = TrackerColors.Grape,
                kicker = "Drag a card into a pile or tap a corner to sort.",
            )

            // Progress dots
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                queue.forEachIndexed { i, _ ->
                    val pct = idx % queue.size
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    i < pct -> TrackerColors.Mint
                                    i == pct -> TrackerColors.Butter
                                    else -> TrackerColors.Paper2
                                },
                                RoundedCornerShape(3.dp),
                            )
                            .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(3.dp)),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Card stage
            Box(
                modifier = Modifier
                    .padding(horizontal = 22.dp)
                    .fillMaxWidth()
                    .height(360.dp),
            ) {
                // Category targets at edges
                CatLabel("Need",  TrackerColors.Mint,  modifier = Modifier.align(Alignment.CenterStart), lit = intent == 'l', hint = "needs, bills")
                CatLabel("Want",  TrackerColors.Coral, modifier = Modifier.align(Alignment.CenterEnd),   lit = intent == 'r', hint = "fun, dining")
                CatLabel("Save",  TrackerColors.Mint,  modifier = Modifier.align(Alignment.TopCenter),   lit = intent == 'u', hint = "goals")
                CatLabel("Skip",  TrackerColors.Paper3,modifier = Modifier.align(Alignment.BottomCenter),lit = intent == 'd', hint = "reimburse")

                // Back cards
                StickerCard(bgColor = TrackerColors.PaperWhite, modifier = Modifier.size(220.dp, 280.dp).align(Alignment.Center).rotate(-2.5f).graphicsLayer { alpha = 0.4f }, tilt = 0f, cornerRadius = 18.dp, borderWidth = 1.8.dp) {}
                StickerCard(bgColor = TrackerColors.PaperWhite, modifier = Modifier.size(230.dp, 288.dp).align(Alignment.Center).rotate(1.6f).graphicsLayer { alpha = 0.65f }, tilt = 0f, cornerRadius = 18.dp, borderWidth = 1.8.dp) {}

                // Front card — draggable
                Box(
                    modifier = Modifier
                        .size(240.dp, 300.dp)
                        .align(Alignment.Center)
                        .offset { IntOffset(cardOffsetX.roundToInt(), cardOffsetY.roundToInt()) }
                        .rotate(cardRotation)
                        .pointerInput(idx) {
                            detectDragGestures(
                                onDragEnd = {
                                    val dir = when {
                                        abs(dragX) > abs(dragY) -> if (dragX > threshold) 'r' else if (dragX < -threshold) 'l' else null
                                        else -> if (dragY < -threshold) 'u' else if (dragY > threshold) 'd' else null
                                    }
                                    if (dir != null) {
                                        idx++
                                        dragX = 0f
                                        dragY = 0f
                                    } else {
                                        dragX = 0f
                                        dragY = 0f
                                    }
                                },
                                onDragCancel = { dragX = 0f; dragY = 0f },
                            ) { _, dragAmount ->
                                dragX += dragAmount.x
                                dragY += dragAmount.y
                            }
                        }
                        .popShadow(cornerRadius = 20.dp, offsetX = 4.dp, offsetY = 5.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(TrackerColors.PaperWhite, RoundedCornerShape(20.dp))
                        .border(2.dp, TrackerColors.Ink, RoundedCornerShape(20.dp)),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(card.timestamp, fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.2.sp, color = TrackerColors.Ink2.copy(alpha = 0.6f))
                            Box(
                                modifier = Modifier.size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TrackerColors.Butter, RoundedCornerShape(12.dp))
                                    .border(1.6.dp, TrackerColors.Ink, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) { Text(card.glyph, fontSize = 22.sp) }
                        }
                        Column {
                            Text(card.merchant, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink, lineHeight = 28.sp)
                            Text("−${"$"}${card.amount}", fontSize = 52.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink, lineHeight = 50.sp)
                            Text(card.hint, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.7f), letterSpacing = 0.5.sp)
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .height(1.dp)
                                .background(TrackerColors.Ink.copy(alpha = 0.2f)),
                        )
                    }

                    // Intent stamp overlay
                    if (intent != null) {
                        val stampLabel = cats.find { it.dir == intent }?.label ?: ""
                        Box(
                            modifier = Modifier
                                .align(
                                    when (intent) {
                                        'l' -> Alignment.CenterStart
                                        'r' -> Alignment.CenterEnd
                                        'u' -> Alignment.TopCenter
                                        else -> Alignment.BottomCenter
                                    }
                                )
                                .padding(18.dp)
                                .graphicsLayer { alpha = stampAlpha }
                                .rotate(-12f)
                                .border(2.5.dp, TrackerColors.Tangerine, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                        ) {
                            Text(stampLabel.uppercase(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TrackerColors.Tangerine, letterSpacing = 2.sp)
                        }
                    }
                }
            }

            // Legend
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "← need · → want · ↑ save · ↓ skip",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp,
                    color = TrackerColors.Ink2.copy(alpha = 0.6f),
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(TrackerColors.Paper2, RoundedCornerShape(999.dp))
                        .border(1.5.dp, TrackerColors.Ink, RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Text("+ Manual", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
                }
            }
        }

        TrackerTabBar(active = null, onTabClick = onTabClick, onAddClick = onAddClick, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun CatLabel(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    lit: Boolean = false,
    hint: String = "",
) {
    val scale by animateFloatAsState(if (lit) 1.1f else 1f, animationSpec = tween(150))
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .popShadow(cornerRadius = 14.dp, offsetX = 2.dp, offsetY = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (lit) color else TrackerColors.Paper2, RoundedCornerShape(14.dp))
            .border(1.6.dp, TrackerColors.Ink, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
            Text(hint, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.65f), letterSpacing = 0.4.sp)
        }
    }
}
