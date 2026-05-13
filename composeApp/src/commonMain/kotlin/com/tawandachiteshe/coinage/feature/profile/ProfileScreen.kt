package com.tawandachiteshe.coinage.feature.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.TrackerScaffold
import com.tawandachiteshe.coinage.ui.components.TrackerTab
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.TrackerColors

@Composable
fun ProfileScreen(
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
    onOpenSettings: () -> Unit,
) {
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
                    // Tape strip
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 10.dp)
                            .size(64.dp, 18.dp)
                            .rotate(-6f)
                            .background(Color(0xB3FF8A4D)),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .rotate(-4f)
                                .clip(CircleShape)
                                .background(TrackerColors.Tangerine)
                                .border(2.dp, TrackerColors.Ink, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("M", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = TrackerColors.Paper)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Maya", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                            Text("steady saver · joined feb '26", fontSize = 16.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Ink2)
                        }
                        Text("★", fontSize = 28.sp, color = TrackerColors.Coral, modifier = Modifier.rotate(14f))
                    }
                }
            }

            // Quick stats
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(
                    Triple("\$24k", "tracked",  TrackerColors.Mint to -1.2f),
                    Triple("3 wk", "streak",   TrackerColors.Coral to 1.4f),
                    Triple("6",    "jars",     TrackerColors.Sky to -0.6f),
                ).forEach { (value, label, colorTilt) ->
                    val (color, tilt) = colorTilt
                    StickerCard(
                        bgColor = color,
                        modifier = Modifier.weight(1f).rotate(tilt),
                        cornerRadius = 14.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                            Text(label.uppercase(), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = TrackerColors.Ink2.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // Badges
            Spacer(Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                Row {
                    Text("Stickers ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                    Text("earned", fontSize = 20.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = TrackerColors.Grape)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(
                        Triple("❄", "first save",     TrackerColors.Sky),
                        Triple("★", "on a roll",      TrackerColors.Butter),
                        Triple("▲", "mountain\nmover",TrackerColors.Coral),
                        Triple("◐", "half full",      TrackerColors.Mint),
                        Triple("?", "?",              TrackerColors.Paper2),
                    ).forEachIndexed { i, (glyph, label, color) ->
                        val locked = i == 4
                        Box(
                            modifier = Modifier
                                .size(64.dp, 80.dp)
                                .rotate(if (i % 2 == 0) -2f else 2f)
                                .popShadow(cornerRadius = 12.dp, offsetX = 2.5.dp, offsetY = 3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(color, RoundedCornerShape(12.dp))
                                .border(1.6.dp, TrackerColors.Ink, RoundedCornerShape(12.dp))
                                .then(if (locked) Modifier.background(color.copy(alpha = 0.45f)) else Modifier),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(glyph, fontSize = 22.sp, color = TrackerColors.Ink)
                                Text(label, fontSize = 9.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp, color = TrackerColors.Ink, lineHeight = 11.sp, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }
                    }
                }
            }

            // Link rows
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(
                    Triple("Account · Maya Rivera",   "Face ID linked",      "◐" to false),
                    Triple("Categories & jars",       "6 active",            "◇" to false),
                    Triple("Export your data",        "JSON or CSV",         "↧" to false),
                    Triple("Open Settings",           "theme, currency, sync","▸" to true),
                ).forEachIndexed { i, (label, hint, glyphCta) ->
                    val (glyph, isCta) = glyphCta
                    StickerCard(
                        bgColor = TrackerColors.PaperWhite,
                        modifier = Modifier.fillMaxWidth().rotate(if (i % 2 == 0) -0.4f else 0.6f),
                        cornerRadius = 14.dp, borderWidth = 1.6.dp, shadowX = 2.5.dp, shadowY = 3.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.size(34.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (isCta) TrackerColors.Tangerine else TrackerColors.Paper2)
                                    .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(9.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(glyph, fontSize = 16.sp, color = TrackerColors.Ink)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TrackerColors.Ink)
                                Text(hint, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp, color = TrackerColors.Ink2.copy(alpha = 0.65f))
                            }
                            Text("›", fontSize = 16.sp, color = TrackerColors.Ink2.copy(alpha = 0.5f))
                        }
                    }
                }
            }
    }
}
