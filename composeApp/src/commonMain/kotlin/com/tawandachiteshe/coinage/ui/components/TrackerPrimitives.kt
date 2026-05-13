package com.tawandachiteshe.coinage.ui.components

import androidx.compose.ui.unit.Constraints
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.tawandachiteshe.coinage.ui.theme.TrackerColors

// Hard pixel shadow matching CSS box-shadow: Xpx Ypx 0 ink
fun Modifier.popShadow(
    shadowColor: Color = TrackerColors.Ink,
    cornerRadius: Dp = 14.dp,
    offsetX: Dp = 3.dp,
    offsetY: Dp = 4.dp,
): Modifier = this
    .layout { measurable, constraints ->
        val oxPx = offsetX.roundToPx()
        val oyPx = offsetY.roundToPx()
        val newMaxW = if (constraints.maxWidth == Constraints.Infinity) Constraints.Infinity
                      else (constraints.maxWidth - oxPx).coerceAtLeast(constraints.minWidth)
        val newMaxH = if (constraints.maxHeight == Constraints.Infinity) Constraints.Infinity
                      else (constraints.maxHeight - oyPx).coerceAtLeast(constraints.minHeight)
        val placeable = measurable.measure(
            constraints.copy(maxWidth = newMaxW, maxHeight = newMaxH)
        )
        layout(
            width = placeable.width + offsetX.roundToPx(),
            height = placeable.height + offsetY.roundToPx(),
        ) {
            placeable.placeRelative(0, 0)
        }
    }
    .drawBehind {
        drawShadowRect(shadowColor, offsetX.toPx(), offsetY.toPx(), cornerRadius.toPx())
    }

private fun DrawScope.drawShadowRect(color: Color, offsetX: Float, offsetY: Float, radius: Float) {
    drawRoundRect(
        color = color,
        topLeft = Offset(offsetX, offsetY),
        size = Size(size.width - offsetX, size.height - offsetY),
        cornerRadius = CornerRadius(radius),
    )
}

fun Modifier.popShadowSm(cornerRadius: Dp = 12.dp) =
    popShadow(cornerRadius = cornerRadius, offsetX = 2.dp, offsetY = 2.dp)

fun Modifier.popShadowLg(cornerRadius: Dp = 18.dp) =
    popShadow(cornerRadius = cornerRadius, offsetX = 5.dp, offsetY = 6.dp)

// Sticker card — coloured rounded box with ink border + hard shadow
@Composable
fun StickerCard(
    bgColor: Color,
    modifier: Modifier = Modifier,
    tilt: Float = 0f,
    cornerRadius: Dp = 14.dp,
    borderWidth: Dp = 1.8.dp,
    shadowX: Dp = 3.dp,
    shadowY: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .rotate(tilt)
            .popShadow(cornerRadius = cornerRadius, offsetX = shadowX, offsetY = shadowY)
            .clip(shape)
            .background(bgColor, shape)
            .border(borderWidth, TrackerColors.Ink, shape),
        content = content,
    )
}

// Tape strip decoration
@Composable
fun TapeStrip(
    modifier: Modifier = Modifier,
    length: Dp = 52.dp,
    height: Dp = 14.dp,
    tilt: Float = -6f,
    color: Color = Color(0xB3FFB84D),
) {
    Spacer(
        modifier = modifier
            .size(length, height)
            .rotate(tilt)
            .background(color)
            .border(0.dp, Color.Transparent),
    )
}

// Vertical progress jar
@Composable
fun ProgressJar(
    pct: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    width: Dp = 28.dp,
) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .size(width, height)
            .clip(shape)
            .background(TrackerColors.Paper, shape)
            .border(1.6.dp, TrackerColors.Ink, shape),
    ) {
        // fill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height * (pct / 100f).coerceIn(0f, 1f))
                .align(Alignment.BottomCenter)
                .background(color)
                .border(0.dp, Color.Transparent),
        )
        // highlight
        Box(
            modifier = Modifier
                .padding(start = 4.dp, top = 4.dp)
                .size(4.dp, height * 0.6f)
                .background(Color.White.copy(alpha = 0.45f), RoundedCornerShape(2.dp)),
        )
    }
}

// Category chip badge
@Composable
fun CategoryChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    small: Boolean = false,
) {
    val fontSize = if (small) 11.sp else 13.sp
    val iconSize = if (small) 12.dp else 14.dp
    val padding = if (small) 4.dp to 8.dp else 5.dp to 10.dp
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = modifier
            .popShadowSm(cornerRadius = 999.dp)
            .clip(shape)
            .background(color, shape)
            .border(1.4.dp, TrackerColors.Ink, shape)
            .padding(vertical = padding.first, horizontal = padding.second),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = TrackerColors.Ink,
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Default,
            color = TrackerColors.Ink,
        )
    }
}

// Dashed progress bar
@Composable
fun StripedProgressBar(
    pct: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    cornerRadius: Dp = 999.dp,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(TrackerColors.Paper2, shape)
            .border(1.5.dp, TrackerColors.Ink, shape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = (pct / 100f).coerceIn(0.03f, 1f))
                .height(height)
                .background(color)
                .drawBehind {
                    // simple stripe overlay
                },
        )
        Text(
            text = "${pct.toInt()}%",
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TrackerColors.Ink,
        )
    }
}

// Page header used across screens
@Composable
fun PageHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    italicWord: String? = null,
    accent: Color = TrackerColors.Tangerine,
    kicker: String? = null,
) {
    Box(modifier = modifier.padding(horizontal = 22.dp, vertical = 4.dp)) {
        Column {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    fontSize = 11.sp,
                    letterSpacing = 1.8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TrackerColors.Ink2.copy(alpha = 0.7f),
                )
            }
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = title,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp,
                    color = TrackerColors.Ink,
                )
                if (italicWord != null) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = italicWord,
                        fontSize = 34.sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 34.sp,
                        color = accent,
                    )
                }
            }
            if (kicker != null) {
                Text(
                    text = kicker,
                    modifier = Modifier.padding(top = 6.dp),
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = TrackerColors.Ink2,
                )
            }
        }
    }
}

// Ink-on-paper receipt row
@Composable
fun ReceiptRow(
    merchant: String,
    amount: String,
    category: String,
    time: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    tilt: Float = 0f,
) {
    val shape = RoundedCornerShape(8.dp)
    StickerCard(
        bgColor = TrackerColors.PaperWhite,
        modifier = modifier,
        tilt = tilt,
        cornerRadius = 8.dp,
        borderWidth = 1.6.dp,
        shadowX = 3.dp,
        shadowY = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor, RoundedCornerShape(8.dp))
                    .border(1.4.dp, TrackerColors.Ink, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = TrackerColors.Ink)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = merchant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrackerColors.Ink,
                    maxLines = 1,
                )
                Text(
                    text = "$category · $time",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TrackerColors.Ink2.copy(alpha = 0.65f),
                    letterSpacing = 0.5.sp,
                )
            }
            Text(
                text = "−\$$amount",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TrackerColors.Ink,
            )
        }
    }
}

@Composable
fun TrackerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingText: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = when {
            isError   -> TrackerColors.Cherry
            isFocused -> TrackerColors.Tangerine
            else      -> TrackerColors.Ink
        },
        animationSpec = tween(150),
    )
    val shape = RoundedCornerShape(12.dp)
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp,
            color = TrackerColors.Ink2.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(5.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            textStyle = TextStyle(
                color = TrackerColors.Ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            cursorBrush = SolidColor(TrackerColors.Tangerine),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(TrackerColors.PaperWhite, shape)
                        .border(1.8.dp, borderColor, shape)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leadingText != null) {
                        Text(
                            text = leadingText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrackerColors.Ink2,
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 15.sp,
                                color = TrackerColors.Ink2.copy(alpha = 0.45f),
                            )
                        }
                        innerTextField()
                    }
                }
            },
        )
    }
}