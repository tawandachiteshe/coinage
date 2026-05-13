package com.tawandachiteshe.coinage.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.TrackerScaffold
import com.tawandachiteshe.coinage.ui.components.TrackerTab
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import com.tawandachiteshe.coinage.ui.theme.TrackerIcons
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onManageJars: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    TrackerScaffold(activeTab = null, onTabClick = onTabClick, onAddClick = onAddClick) {
        Text(
            text = "Profile".uppercase(),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.4.sp,
            color = TrackerColors.Ink2.copy(alpha = 0.7f),
        )

        // Hero ID card
        Spacer(Modifier.height(14.dp))
        StickerCard(
            bgColor = TrackerColors.Butter,
            modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
            cornerRadius = 22.dp, borderWidth = 2.dp, shadowX = 4.dp, shadowY = 5.dp,
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 10.dp)
                        .size(64.dp, 18.dp)
                        .rotate(-6f)
                        .background(Color(0xB3FF8A4D)),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .rotate(-4f)
                            .clip(CircleShape)
                            .background(TrackerColors.Tangerine)
                            .border(2.dp, TrackerColors.Ink, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(state.initial, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = TrackerColors.Paper)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(state.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                        Text("steady saver · joined feb '26", fontSize = 16.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Ink2)
                    }
                    Text("★", fontSize = 28.sp, color = TrackerColors.Coral, modifier = Modifier.rotate(14f))
                }
            }
        }

        // Quick stats — live data
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            data class Stat(val value: String, val label: String, val color: Color, val tilt: Float)
            listOf(
                Stat(state.totalTrackedLabel, "tracked", TrackerColors.Mint,  -1.2f),
                Stat("${state.txCount}",      "txns",    TrackerColors.Coral,  1.4f),
                Stat("${state.jarCount}",     "jars",    TrackerColors.Sky,   -0.6f),
            ).forEach { stat ->
                StickerCard(
                    bgColor = stat.color,
                    modifier = Modifier.weight(1f).rotate(stat.tilt),
                    cornerRadius = 14.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stat.value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                        Text(stat.label.uppercase(), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = TrackerColors.Ink2.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // Stickers earned — badges unlock based on real data
        Spacer(Modifier.height(24.dp))
        Column(modifier = Modifier.padding(horizontal = 22.dp)) {
            Row {
                Text("Stickers ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                Text("earned", fontSize = 20.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Grape)
            }
            Spacer(Modifier.height(10.dp))
            data class Badge(val glyph: String, val label: String, val color: Color, val unlocked: Boolean)
            val badges = listOf(
                Badge("❄", "first save",      TrackerColors.Sky,    state.hasFirstSave),
                Badge("★", "on a roll",       TrackerColors.Butter, state.hasOnARoll),
                Badge("▲", "mountain\nmover", TrackerColors.Coral,  state.hasMountainMover),
                Badge("◐", "half full",       TrackerColors.Mint,   state.hasHalfFull),
                Badge("?", "locked",          TrackerColors.Paper2, false),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                badges.forEachIndexed { i, badge ->
                    Box(
                        modifier = Modifier
                            .size(64.dp, 80.dp)
                            .rotate(if (i % 2 == 0) -2f else 2f)
                            .then(if (badge.unlocked) Modifier.popShadow(cornerRadius = 12.dp, offsetX = 2.5.dp, offsetY = 3.dp) else Modifier)
                            .clip(RoundedCornerShape(12.dp))
                            .background(badge.color, RoundedCornerShape(12.dp))
                            .border(1.6.dp, TrackerColors.Ink.copy(alpha = if (badge.unlocked) 1f else 0.35f), RoundedCornerShape(12.dp))
                            .alpha(if (badge.unlocked) 1f else 0.38f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(badge.glyph, fontSize = 22.sp, color = TrackerColors.Ink)
                            Text(badge.label, fontSize = 9.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp, color = TrackerColors.Ink, lineHeight = 11.sp, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
            }
        }

        // Link rows
        Spacer(Modifier.height(24.dp))
        data class ProfileLink(val label: String, val hint: String, val icon: ImageVector, val isCta: Boolean = false, val onClick: () -> Unit = {})
        val links = listOf(
            ProfileLink("Account · ${state.name}", "Face ID linked",                TrackerIcons.User),
            ProfileLink("Categories & jars",       "${state.jarCount} active",      TrackerIcons.Layers,   onClick = onManageJars),
            ProfileLink("Export your data",        "JSON or CSV",                   TrackerIcons.Download),
            ProfileLink("Open Settings",           "theme, currency, sync",         TrackerIcons.Settings, isCta = true, onClick = onOpenSettings),
        )
        Column(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            links.forEachIndexed { i, link ->
                StickerCard(
                    bgColor = TrackerColors.PaperWhite,
                    modifier = Modifier.fillMaxWidth().rotate(if (i % 2 == 0) -0.4f else 0.6f),
                    cornerRadius = 14.dp, borderWidth = 1.6.dp, shadowX = 2.5.dp, shadowY = 3.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { link.onClick() }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (link.isCta) TrackerColors.Tangerine else TrackerColors.Paper2)
                                .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(link.icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = TrackerColors.Ink)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(link.label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
                            Text(link.hint, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp, color = TrackerColors.Ink2.copy(alpha = 0.65f))
                        }
                        Icon(TrackerIcons.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = TrackerColors.Ink2.copy(alpha = 0.5f))
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}