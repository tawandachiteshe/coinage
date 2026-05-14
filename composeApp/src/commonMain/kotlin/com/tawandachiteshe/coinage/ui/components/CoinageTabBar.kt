package com.tawandachiteshe.coinage.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tawandachiteshe.coinage.ui.theme.CoinageColors
import com.tawandachiteshe.coinage.ui.theme.CoinageIcons

enum class CoinageTab { Home, Goals, Debt, Insights }

@Composable
fun CoinageTabBar(
    active: CoinageTab?,
    onTabClick: (CoinageTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs: List<Pair<CoinageTab, Pair<ImageVector, String>>> = listOf(
        CoinageTab.Home     to (CoinageIcons.Home       to "Home"),
        CoinageTab.Goals    to (CoinageIcons.Target     to "Goals"),
        CoinageTab.Debt     to (CoinageIcons.CreditCard to "Debts"),
        CoinageTab.Insights to (CoinageIcons.BarChart   to "Recap"),
    )

    // FAB breathing — ring pulses outward 24dp and fades
    val breathe = rememberInfiniteTransition(label = "fab_breathe")
    val breatheRadius by breathe.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe_radius",
    )

    Box(modifier = modifier) {
        // FAB — tangerine circle with breathing ring behind it
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-78).dp)
                .size(60.dp)
                .drawBehind {
                    // ring expands from FAB edge outward by 24dp as breatheRadius goes 0→1
                    val fabRadius = size.minDimension / 2f
                    val ringRadius = fabRadius + breatheRadius * 24.dp.toPx()
                    val ringAlpha  = 0.45f * (1f - breatheRadius)
                    drawCircle(
                        color = CoinageColors.Tangerine.copy(alpha = ringAlpha),
                        radius = ringRadius,
                    )
                }
                .popShadow(cornerRadius = 30.dp, offsetX = 4.dp, offsetY = 5.dp)
                .clip(CircleShape)
                .background(CoinageColors.Tangerine, CircleShape)
                .border(2.dp, CoinageColors.Ink, CircleShape)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = CoinageIcons.Add,
                contentDescription = "Add transaction",
                tint = CoinageColors.Paper,
                modifier = Modifier.size(26.dp),
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
                .background(CoinageColors.Paper, RoundedCornerShape(22.dp))
                .border(2.dp, CoinageColors.Ink, RoundedCornerShape(22.dp)),
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

                    // spring pop for the active indicator
                    val indicatorScale by animateFloatAsState(
                        targetValue = if (isActive) 1f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                        label = "indicator_${tab.name}",
                    )
                    // icon/label opacity cross-fade
                    val contentAlpha by animateFloatAsState(
                        targetValue = if (isActive) 1f else 0.55f,
                        animationSpec = tween(180),
                        label = "alpha_${tab.name}",
                    )

                    Column(
                        modifier = Modifier
                            .clickable { onTabClick(tab) }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            // Butter sticker — scales in with spring when active
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .graphicsLayer {
                                        scaleX = indicatorScale
                                        scaleY = indicatorScale
                                    }
                                    .popShadowSm(cornerRadius = 9.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(CoinageColors.Butter, RoundedCornerShape(9.dp))
                                    .border(1.4.dp, CoinageColors.Ink, RoundedCornerShape(9.dp)),
                            )
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = CoinageColors.Ink2.copy(alpha = contentAlpha),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = label.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp,
                            color = CoinageColors.Ink2.copy(alpha = contentAlpha),
                        )
                    }
                }
            }
        }
    }
}