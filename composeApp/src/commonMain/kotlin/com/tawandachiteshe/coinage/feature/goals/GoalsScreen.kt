package com.tawandachiteshe.coinage.feature.goals

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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tawandachiteshe.coinage.ui.components.PageHeader
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.TrackerTextField
import com.tawandachiteshe.coinage.ui.components.StripedProgressBar
import com.tawandachiteshe.coinage.ui.components.TrackerTab
import com.tawandachiteshe.coinage.ui.components.TrackerTabBar
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import com.tawandachiteshe.coinage.ui.theme.TrackerIcons
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

private val GOAL_COLORS = listOf(
    TrackerColors.Sky, TrackerColors.Mint, TrackerColors.Grape,
    TrackerColors.Coral, TrackerColors.Butter, TrackerColors.Tangerine,
)
private val GOAL_TILTS = listOf(-1.4f, 0.9f, -0.6f, 1.4f, -0.8f, 0.5f)

@Composable
fun GoalsScreen(
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
    viewModel: GoalsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

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
                eyebrow = "Goals · ${state.goals.size} in flight",
                title = "Saving",
                italicWord = "for…",
                accent = TrackerColors.Grape,
                kicker = "Each jar fills with every contribution. Tap a goal to nudge it forward.",
            )

            // Summary sticker
            val overallPct = if (state.totalTarget > 0) ((state.totalSaved / state.totalTarget) * 100).toInt() else 0
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
                        Text("$overallPct%", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TrackerColors.Ink)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("\$${state.totalSaved.fmtWhole()} of \$${state.totalTarget.fmtWhole()} saved", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
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
                if (state.goals.isEmpty() && !state.isLoading) {
                    Text(
                        text = "No goals yet — plant one below.",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TrackerColors.Ink2.copy(alpha = 0.55f),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
                state.goals.forEachIndexed { i, goal ->
                    val color = GOAL_COLORS[i % GOAL_COLORS.size]
                    val tilt = GOAL_TILTS[i % GOAL_TILTS.size]
                    StickerCard(
                        bgColor = TrackerColors.PaperWhite,
                        modifier = Modifier.fillMaxWidth(),
                        tilt = tilt,
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
                                            .background(color, RoundedCornerShape(12.dp))
                                            .border(1.6.dp, TrackerColors.Ink, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) { Icon(imageVector = TrackerIcons.fromKey(goal.icon), contentDescription = null, modifier = Modifier.size(22.dp), tint = TrackerColors.Ink) }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(goal.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                        if (goal.due != null) Text(goal.due, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp, color = TrackerColors.Ink2.copy(alpha = 0.65f))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("\$${goal.savedAmount.fmtWhole()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                                        Text("/\$${goal.targetAmount.fmtWhole()}", fontSize = 12.sp, color = TrackerColors.Ink2.copy(alpha = 0.55f))
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                StripedProgressBar(pct = goal.pct, color = color, modifier = Modifier.fillMaxWidth())

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
                                                .clickable { viewModel.onAction(GoalsAction.OnAddContribution(goal.id, amt.toDouble())) }
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                        ) {
                                            Text("+ \$$amt", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
                                        }
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Text("${goal.pct.toInt()}%", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.65f))
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
                        .border(2.dp, TrackerColors.Ink, RoundedCornerShape(18.dp))
                        .clickable { showAddDialog = true },
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

        if (showAddDialog) {
            AddGoalDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, target ->
                    viewModel.onAction(GoalsAction.OnCreateGoal(name = name, icon = "target", targetAmount = target, deadlineMs = null))
                    showAddDialog = false
                },
            )
        }

        TrackerTabBar(active = TrackerTab.Goals, onTabClick = onTabClick, onAddClick = onAddClick, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun AddGoalDialog(onDismiss: () -> Unit, onConfirm: (name: String, target: Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        StickerCard(bgColor = TrackerColors.Paper, cornerRadius = 20.dp, shadowX = 4.dp, shadowY = 5.dp) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("New goal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                TrackerTextField(value = name, onValueChange = { name = it }, label = "Goal name", modifier = Modifier.fillMaxWidth())
                TrackerTextField(value = target, onValueChange = { target = it }, label = "Target amount", leadingText = "$", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp)) { Text("Cancel", color = TrackerColors.Ink2) }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TrackerColors.Ink)
                            .clickable { onConfirm(name, target.toDoubleOrNull() ?: 0.0) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) { Text("Save", color = TrackerColors.Paper, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

private fun Double.fmtWhole(): String {
    val l = kotlin.math.abs(toLong())
    return if (l >= 1000) "${l / 1000},${(l % 1000).toString().padStart(3, '0')}" else l.toString()
}
