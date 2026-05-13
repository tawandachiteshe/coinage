package com.tawandachiteshe.coinage.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.TrackerColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class Step(
    val eyebrow: String,
    val title: String,
    val italicWord: String,
    val accent: Color,
    val body: String,
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val steps = remember {
        listOf(
            Step("welcome", "Money,", "but fun.", TrackerColors.Tangerine,
                "Tracker is a sticker book for your spending. Toss receipts into piles, watch goal jars fill, climb the debt mountain — one small step at a time."),
            Step("how it works", "Swipe to", "sort.", TrackerColors.Grape,
                "New transactions land in an inbox. Swipe each card — left for Need, right for Want, up to Save, down to Skip. That's the whole input loop."),
            Step("private by default", "Stays on", "your phone.", TrackerColors.Mint,
                "Locked behind Face ID. No accounts, no email, no servers. AI assistants are opt-in later — for now, just you and the numbers."),
        )
    }
    var step by remember { mutableIntStateOf(0) }
    val s = steps[step]
    val isLast = step == steps.lastIndex

    // CTA button scale pops on step change
    val ctaScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cta_scale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrackerColors.Paper),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 64.dp, bottom = 130.dp),
        ) {
            // Progress dots — color animates, position stays fixed
            Row(
                modifier = Modifier
                    .padding(horizontal = 22.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                steps.forEachIndexed { i, _ ->
                    val dotColor by animateColorAsState(
                        targetValue = if (i <= step) s.accent else TrackerColors.Paper2,
                        animationSpec = tween(300, easing = EaseInOut),
                        label = "dot_$i",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(dotColor, RoundedCornerShape(999.dp))
                            .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(999.dp)),
                    )
                    if (i < steps.lastIndex) Spacer(Modifier.width(8.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${step + 1}/${steps.size}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = TrackerColors.Ink2.copy(alpha = 0.6f),
                )
            }

            // Art + text slides in/out on step change
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val forward = targetState > initialState
                    slideInHorizontally(tween(340, easing = EaseInOut)) { if (forward) it else -it } +
                    fadeIn(tween(220, delayMillis = 80)) togetherWith
                    slideOutHorizontally(tween(280, easing = EaseInOut)) { if (forward) -it else it } +
                    fadeOut(tween(180))
                },
                label = "step_content",
                modifier = Modifier.fillMaxWidth(),
            ) { currentStep ->
                val cs = steps[currentStep]
                Column {
                    // Art
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 22.dp)
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        OnboardingArt(step = currentStep, accent = cs.accent)
                    }

                    // Text
                    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                        Text(
                            text = cs.eyebrow.uppercase(),
                            fontSize = 11.sp,
                            letterSpacing = 1.8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TrackerColors.Ink2.copy(alpha = 0.7f),
                        )
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Text(
                                text = cs.title,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 40.sp,
                                color = TrackerColors.Ink,
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = cs.italicWord,
                                fontSize = 42.sp,
                                fontStyle = FontStyle.Italic,
                                fontFamily = FontFamily.Serif,
                                lineHeight = 40.sp,
                                color = cs.accent,
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = cs.body,
                            fontSize = 15.5.sp,
                            lineHeight = 22.sp,
                            color = TrackerColors.Ink2,
                        )
                    }
                }
            }
        }

        // Skip button
        Text(
            text = "SKIP",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 62.dp, end = 22.dp)
                .clickable { onFinish() },
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            color = TrackerColors.Ink2.copy(alpha = 0.6f),
        )

        // CTA row
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 22.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Back button
            val backAlpha by animateFloatAsState(
                targetValue = if (step > 0) 1f else 0.3f,
                animationSpec = tween(200),
                label = "back_alpha",
            )
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .then(
                        if (step > 0) Modifier
                            .popShadow(cornerRadius = 26.dp)
                            .border(1.8.dp, TrackerColors.Ink, CircleShape)
                        else Modifier.border(1.8.dp, TrackerColors.Ink.copy(alpha = 0.3f), CircleShape)
                    )
                    .clip(CircleShape)
                    .background(TrackerColors.Paper, CircleShape)
                    .clickable(enabled = step > 0) { if (step > 0) step-- }
                    .graphicsLayer { alpha = backAlpha },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "←",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrackerColors.Ink,
                )
            }

            // Next / Finish button — label cross-fades
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .graphicsLayer { scaleX = ctaScale; scaleY = ctaScale }
                    .popShadow(cornerRadius = 28.dp, offsetX = 3.dp, offsetY = 4.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(TrackerColors.Ink, RoundedCornerShape(28.dp))
                    .border(1.8.dp, TrackerColors.Ink, RoundedCornerShape(28.dp))
                    .clickable { if (isLast) onFinish() else step++ },
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = isLast,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                    },
                    label = "cta_label",
                ) { last ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = if (last) "Get started" else "Next",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrackerColors.Paper,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "→",
                            fontSize = 18.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            color = TrackerColors.Butter,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingArt(step: Int, accent: Color) {
    // Each card floats in from below with a staggered spring on first composition
    val offset1 = remember { Animatable(60f) }
    val offset2 = remember { Animatable(80f) }
    val offset3 = remember { Animatable(100f) }
    val alpha1  = remember { Animatable(0f) }
    val alpha2  = remember { Animatable(0f) }
    val alpha3  = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            launch { offset1.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
            alpha1.animateTo(1f, tween(220))
        }
        delay(80)
        launch {
            launch { offset2.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
            alpha2.animateTo(1f, tween(220))
        }
        delay(80)
        launch {
            launch { offset3.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
            alpha3.animateTo(1f, tween(220))
        }
    }

    when (step) {
        0 -> Box(modifier = Modifier.size(260.dp, 200.dp)) {
            StickerCard(
                bgColor = TrackerColors.Butter,
                modifier = Modifier
                    .size(110.dp, 60.dp)
                    .rotate(-6f)
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 38.dp)
                    .graphicsLayer { translationY = offset1.value; alpha = alpha1.value },
                tilt = 0f, cornerRadius = 18.dp,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("$42", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                }
            }
            StickerCard(
                bgColor = TrackerColors.Tangerine,
                modifier = Modifier
                    .size(130.dp, 70.dp)
                    .align(Alignment.TopEnd)
                    .graphicsLayer { translationY = offset2.value; alpha = alpha2.value },
                tilt = 8f, cornerRadius = 18.dp,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("hi 👋", fontSize = 26.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = Color.White)
                }
            }
            StickerCard(
                bgColor = TrackerColors.Grape,
                modifier = Modifier
                    .size(170.dp, 70.dp)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer { translationY = offset3.value; alpha = alpha3.value },
                tilt = -3f, cornerRadius = 18.dp,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$2,847", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("left", fontSize = 16.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = Color.White)
                    }
                }
            }
        }

        1 -> Box(modifier = Modifier.size(260.dp, 220.dp)) {
            // Back card
            Box(
                modifier = Modifier
                    .size(200.dp, 160.dp)
                    .align(Alignment.Center)
                    .rotate(-4f)
                    .graphicsLayer { translationY = offset1.value; alpha = alpha1.value }
                    .popShadow(cornerRadius = 18.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(TrackerColors.PaperWhite, RoundedCornerShape(18.dp))
                    .border(1.8.dp, TrackerColors.Ink, RoundedCornerShape(18.dp)),
            )
            // Front card
            StickerCard(
                bgColor = TrackerColors.PaperWhite,
                modifier = Modifier
                    .size(200.dp, 160.dp)
                    .align(Alignment.Center)
                    .rotate(8f)
                    .graphicsLayer { translationY = offset2.value; alpha = alpha2.value },
                tilt = 0f, cornerRadius = 18.dp, borderWidth = 2.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("today · 12:30pm", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TrackerColors.Ink2.copy(alpha = 0.6f), letterSpacing = 1.sp)
                    Column {
                        Text("Trader Joe's", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                        Text("−\$42.18", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TrackerColors.Ink)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .border(2.dp, TrackerColors.Tangerine, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .rotate(-12f),
                    ) {
                        Text("WANT", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TrackerColors.Tangerine, letterSpacing = 1.sp)
                    }
                }
            }
        }

        else -> {
            StickerCard(
                bgColor = TrackerColors.Mint,
                modifier = Modifier
                    .size(130.dp, 150.dp)
                    .graphicsLayer { translationY = offset1.value; alpha = alpha1.value },
                tilt = -4f, cornerRadius = 32.dp,
            ) {
                Column(
                    Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("🔒", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("FACE ID", fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp, color = TrackerColors.Ink)
                }
            }
        }
    }
}