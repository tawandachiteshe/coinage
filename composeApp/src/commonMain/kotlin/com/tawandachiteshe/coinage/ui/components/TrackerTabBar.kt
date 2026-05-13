package com.tawandachiteshe.coinage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tawandachiteshe.coinage.ui.theme.TrackerColors

enum class TrackerTab { Home, Goals, Debt, Insights }

@Composable
fun TrackerTabBar(
    active: TrackerTab?,
    onTabClick: (TrackerTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        TrackerTab.Home     to ("◐" to "Home"),
        TrackerTab.Goals    to ("★" to "Goals"),
        TrackerTab.Debt     to ("▲" to "Debts"),
        TrackerTab.Insights to ("◇" to "Recap"),
    )

    Box(modifier = modifier) {
        // FAB — tangerine circle, sits above the bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-78).dp)
                .size(60.dp)
                .popShadow(cornerRadius = 30.dp, offsetX = 4.dp, offsetY = 5.dp)
                .clip(CircleShape)
                .background(TrackerColors.Tangerine, CircleShape)
                .border(2.dp, TrackerColors.Ink, CircleShape)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+",
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = TrackerColors.Paper,
                lineHeight = 32.sp,
            )
        }

        // Nav bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 18.dp)
                .fillMaxWidth()
                .popShadow(cornerRadius = 22.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(TrackerColors.Paper, RoundedCornerShape(22.dp))
                .border(2.dp, TrackerColors.Ink, RoundedCornerShape(22.dp)),
        ) {
            // Tape strip centred on top edge
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-9).dp)
                    .size(width = 58.dp, height = 14.dp)
                    .rotate(-3f)
                    .background(Color(0xB3FFB84D)),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                tabs.forEach { (tab, meta) ->
                    val (icon, label) = meta
                    val isActive = tab == active
                    Column(
                        modifier = Modifier
                            .clickable { onTabClick(tab) }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .then(
                                    if (isActive) Modifier
                                        .popShadowSm(cornerRadius = 9.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(TrackerColors.Butter, RoundedCornerShape(9.dp))
                                        .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(9.dp))
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = icon,
                                fontSize = 16.sp,
                                color = TrackerColors.Ink,
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = label.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp,
                            color = if (isActive) TrackerColors.Ink else TrackerColors.Ink2.copy(alpha = 0.55f),
                        )
                    }
                }
            }
        }
    }
}