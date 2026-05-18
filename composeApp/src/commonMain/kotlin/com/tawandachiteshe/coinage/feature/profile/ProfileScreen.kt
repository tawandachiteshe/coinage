package com.tawandachiteshe.coinage.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tawandachiteshe.coinage.ui.components.CoinageDialog
import com.tawandachiteshe.coinage.ui.components.CoinageScaffold
import com.tawandachiteshe.coinage.ui.components.CoinageTab
import com.tawandachiteshe.coinage.ui.components.CoinageTextField
import com.tawandachiteshe.coinage.ui.components.StickerCard
import com.tawandachiteshe.coinage.ui.components.popShadow
import com.tawandachiteshe.coinage.ui.theme.CoinageColors
import com.tawandachiteshe.coinage.ui.theme.CoinageIcons
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    onTabClick: (CoinageTab) -> Unit,
    onAddClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit = {},
    onManageJars: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    var badgeToast by remember { mutableStateOf<ProfileEvent.BadgeEarned?>(null) }
    var exportToast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.BadgeEarned -> badgeToast = event
                is ProfileEvent.ExportReady -> {
                    clipboard.setText(AnnotatedString(event.csv))
                    exportToast = "Copied ${event.csv.lines().size - 2} rows to clipboard"
                }
            }
        }
    }
    LaunchedEffect(badgeToast) {
        if (badgeToast != null) { delay(3_000); badgeToast = null }
    }
    LaunchedEffect(exportToast) {
        if (exportToast != null) { delay(2_500); exportToast = null }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    CoinageScaffold(activeTab = null, onTabClick = onTabClick, onAddClick = onAddClick, onBack = onBack) {
        Text(
            text = "Profile".uppercase(),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.4.sp,
            color = CoinageColors.Ink2.copy(alpha = 0.7f),
        )

        // Hero ID card
        Spacer(Modifier.height(14.dp))
        StickerCard(
            bgColor = CoinageColors.Butter,
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
                            .background(CoinageColors.Tangerine)
                            .border(2.dp, CoinageColors.Ink, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(state.initial, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = CoinageColors.Paper)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(state.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CoinageColors.Ink)
                        Text("steady saver · joined feb '26", fontSize = 16.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = CoinageColors.Ink2)
                    }
                    Text("★", fontSize = 28.sp, color = CoinageColors.Coral, modifier = Modifier.rotate(14f))
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
                Stat(state.totalTrackedLabel, "tracked", CoinageColors.Mint,  -1.2f),
                Stat("${state.txCount}",      "txns",    CoinageColors.Coral,  1.4f),
                Stat("${state.jarCount}",     "jars",    CoinageColors.Sky,   -0.6f),
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
                        Text(stat.value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CoinageColors.Ink)
                        Text(stat.label.uppercase(), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp, color = CoinageColors.Ink2.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // Stickers earned — badges unlock based on real data
        Spacer(Modifier.height(24.dp))
        Column(modifier = Modifier.padding(horizontal = 22.dp)) {
            Row {
                Text("Stickers ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CoinageColors.Ink)
                Text("earned", fontSize = 20.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Serif, color = CoinageColors.Grape)
            }
            Spacer(Modifier.height(10.dp))
            data class Badge(
                val icon: ImageVector, val label: String,
                val color: Color, val tint: Color, val unlocked: Boolean,
            )
            val allBadges = listOf(
                Badge(CoinageIcons.Snowflake, "first save",      CoinageColors.Sky,       CoinageColors.Ink,   state.hasFirstSave),
                Badge(CoinageIcons.Star,      "on a roll",       CoinageColors.Butter,    CoinageColors.Ink,   state.hasOnARoll),
                Badge(CoinageIcons.Mountain,  "mountain\nmover", CoinageColors.Coral,     CoinageColors.Ink,   state.hasMountainMover),
                Badge(CoinageIcons.PiggyBank, "half full",       CoinageColors.Mint,      CoinageColors.Ink,   state.hasHalfFull),
                Badge(CoinageIcons.Flame,     "streak\nkeeper",  CoinageColors.Grape,     CoinageColors.Paper, state.hasStreakKeeper),
                Badge(CoinageIcons.TrendingUp,"big spender",     CoinageColors.Coral,     CoinageColors.Ink,   state.hasBigSpender),
                Badge(CoinageIcons.Calendar,  "long hauler",     CoinageColors.Sky,       CoinageColors.Ink,   state.hasLongHauler),
                Badge(CoinageIcons.Layers,    "jar master",      CoinageColors.Tangerine, CoinageColors.Ink,   state.hasJarMaster),
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                allBadges.chunked(4).forEachIndexed { rowIdx, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        row.forEachIndexed { colIdx, badge ->
                            val i = rowIdx * 4 + colIdx
                            val tilt = if (i % 2 == 0) -2f else 2f
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(66.dp, 82.dp)
                                        .rotate(tilt)
                                        .alpha(if (badge.unlocked) 1f else 0.3f)
                                        .popShadow(cornerRadius = 12.dp, offsetX = 2.5.dp, offsetY = 3.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(badge.color, RoundedCornerShape(12.dp))
                                        .border(1.6.dp, CoinageColors.Ink, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(horizontal = 5.dp),
                                    ) {
                                        Icon(badge.icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = badge.tint)
                                        Spacer(Modifier.height(5.dp))
                                        Text(
                                            text = badge.label,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 0.3.sp,
                                            color = badge.tint,
                                            lineHeight = 11.sp,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Link rows
        Spacer(Modifier.height(24.dp))
        data class ProfileLink(val label: String, val hint: String, val icon: ImageVector, val isCta: Boolean = false, val onClick: () -> Unit = {})
        val links = listOf(
            ProfileLink("Account · ${state.name}", "tap to edit name",              CoinageIcons.User,     onClick = { viewModel.onAction(ProfileAction.ShowEditName) }),
            ProfileLink("Categories & jars",       "${state.jarCount} active",      CoinageIcons.Layers,   onClick = onManageJars),
            ProfileLink("Export your data",        "CSV · copied to clipboard",     CoinageIcons.Download, onClick = { viewModel.onAction(ProfileAction.ExportData) }),
            ProfileLink("Open Settings",           "theme, currency, sync",         CoinageIcons.Settings, isCta = true, onClick = onOpenSettings),
        )
        Column(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            links.forEachIndexed { i, link ->
                StickerCard(
                    bgColor = CoinageColors.PaperWhite,
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
                                .background(if (link.isCta) CoinageColors.Tangerine else CoinageColors.Paper2)
                                .border(1.4.dp, CoinageColors.Ink, RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(link.icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = CoinageColors.Ink)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(link.label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CoinageColors.Ink)
                            Text(link.hint, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp, color = CoinageColors.Ink2.copy(alpha = 0.65f))
                        }
                        Icon(CoinageIcons.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = CoinageColors.Ink2.copy(alpha = 0.5f))
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    AnimatedVisibility(
        visible = badgeToast != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp, start = 22.dp, end = 22.dp),
    ) {
        badgeToast?.let { toast -> BadgeToastCard(toast.label) }
    }

    AnimatedVisibility(
        visible = exportToast != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp, start = 22.dp, end = 22.dp),
    ) {
        exportToast?.let { msg -> ExportToastCard(msg) }
    }
    } // end outer Box

    if (state.showEditName) {
        CoinageDialog(
            title = "Edit your name",
            onDismissRequest = { viewModel.onAction(ProfileAction.DismissEditName) },
            confirmLabel = "Save",
            confirmColor = com.tawandachiteshe.coinage.ui.theme.CoinageColors.Mint,
            onConfirm = { viewModel.onAction(ProfileAction.SaveName) },
            onDismiss = { viewModel.onAction(ProfileAction.DismissEditName) },
        ) {
            CoinageTextField(
                value = state.editNameValue,
                onValueChange = { viewModel.onAction(ProfileAction.OnEditNameChange(it)) },
                label = "Name",
                placeholder = "Your name",
            )
        }
    }
}

@Composable
private fun BadgeToastCard(label: String) {
    StickerCard(
        bgColor = CoinageColors.Butter,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        borderWidth = 2.dp,
        shadowX = 4.dp,
        shadowY = 5.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .popShadow(cornerRadius = 10.dp, offsetX = 2.dp, offsetY = 2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CoinageColors.Tangerine)
                    .border(1.6.dp, CoinageColors.Ink, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = badgeIconFor(label),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = CoinageColors.Paper,
                )
            }
            Column {
                Text(
                    text = "sticker unlocked!".uppercase(),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = CoinageColors.Ink2.copy(alpha = 0.6f),
                )
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoinageColors.Ink,
                )
            }
        }
    }
}

@Composable
private fun ExportToastCard(message: String) {
    StickerCard(
        bgColor = CoinageColors.Mint,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        borderWidth = 2.dp,
        shadowX = 4.dp,
        shadowY = 5.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(CoinageIcons.Download, contentDescription = null, modifier = Modifier.size(20.dp), tint = CoinageColors.Ink)
            Text(message, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CoinageColors.Ink)
        }
    }
}

private fun badgeIconFor(label: String): ImageVector = when (label) {
    "first save"     -> CoinageIcons.Snowflake
    "on a roll"      -> CoinageIcons.Star
    "mountain mover" -> CoinageIcons.Mountain
    "half full"      -> CoinageIcons.PiggyBank
    "streak keeper"  -> CoinageIcons.Flame
    "big spender"    -> CoinageIcons.TrendingUp
    "long hauler"    -> CoinageIcons.Calendar
    "jar master"     -> CoinageIcons.Layers
    else             -> CoinageIcons.Star
}