package com.tawandachiteshe.coinage.ui.theme

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.ArrowUpRight
import com.composables.icons.lucide.Banknote
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Briefcase
import com.composables.icons.lucide.Bus
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Car
import com.composables.icons.lucide.ChartBar
import com.composables.icons.lucide.CirclePlus
import com.composables.icons.lucide.Clapperboard
import com.composables.icons.lucide.Coffee
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.Droplets
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Gem
import com.composables.icons.lucide.GraduationCap
import com.composables.icons.lucide.Hand
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Keyboard
import com.composables.icons.lucide.Laptop
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mountain
import com.composables.icons.lucide.Music
import com.composables.icons.lucide.PiggyBank
import com.composables.icons.lucide.Plane
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Receipt
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.ShoppingBag
import com.composables.icons.lucide.ShoppingCart
import com.composables.icons.lucide.Snowflake
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Store
import com.composables.icons.lucide.Tag
import com.composables.icons.lucide.Target
import com.composables.icons.lucide.TrendingUp
import com.composables.icons.lucide.Utensils
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.User
import com.composables.icons.lucide.Wallet
import com.composables.icons.lucide.Wifi
import com.composables.icons.lucide.Archive
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X
import com.composables.icons.lucide.Zap

object CoinageIcons {

    // ── Finance ──────────────────────────────────────────────────────────────
    val Wallet: ImageVector      = Lucide.Wallet
    val Banknote: ImageVector    = Lucide.Banknote
    val CreditCard: ImageVector  = Lucide.CreditCard
    val PiggyBank: ImageVector   = Lucide.PiggyBank
    val TrendingUp: ImageVector  = Lucide.TrendingUp
    val Receipt: ImageVector     = Lucide.Receipt

    // ── Shopping ─────────────────────────────────────────────────────────────
    val ShoppingCart: ImageVector = Lucide.ShoppingCart
    val ShoppingBag: ImageVector  = Lucide.ShoppingBag
    val Store: ImageVector        = Lucide.Store

    // ── Food & drink ─────────────────────────────────────────────────────────
    val Utensils: ImageVector = Lucide.Utensils
    val Coffee: ImageVector   = Lucide.Coffee

    // ── Transport ────────────────────────────────────────────────────────────
    val Car: ImageVector   = Lucide.Car
    val Bus: ImageVector   = Lucide.Bus
    val Plane: ImageVector = Lucide.Plane

    // ── Health & wellness ────────────────────────────────────────────────────
    val Heart: ImageVector    = Lucide.Heart
    val Dumbbell: ImageVector = Lucide.Dumbbell
    val Sparkles: ImageVector = Lucide.Sparkles

    // ── Home & utilities ─────────────────────────────────────────────────────
    val Home: ImageVector      = Lucide.House
    val Zap: ImageVector       = Lucide.Zap
    val Droplets: ImageVector  = Lucide.Droplets
    val Wifi: ImageVector      = Lucide.Wifi

    // ── Work & education ─────────────────────────────────────────────────────
    val Briefcase: ImageVector    = Lucide.Briefcase
    val Laptop: ImageVector       = Lucide.Laptop
    val GraduationCap: ImageVector = Lucide.GraduationCap

    // ── Entertainment ────────────────────────────────────────────────────────
    val Clapperboard: ImageVector = Lucide.Clapperboard
    val Music: ImageVector        = Lucide.Music
    val Play: ImageVector         = Lucide.Play

    // ── Goals & tracking ─────────────────────────────────────────────────────
    val Target: ImageVector    = Lucide.Target
    val Star: ImageVector      = Lucide.Star
    val Snowflake: ImageVector = Lucide.Snowflake
    val Shield: ImageVector    = Lucide.Shield
    val Keyboard: ImageVector  = Lucide.Keyboard
    val Gem: ImageVector       = Lucide.Gem
    val Mountain: ImageVector  = Lucide.Mountain
    val Flame: ImageVector     = Lucide.Flame

    // ── Navigation & misc ────────────────────────────────────────────────────
    val ArrowUpRight: ImageVector  = Lucide.ArrowUpRight
    val Lock: ImageVector          = Lucide.Lock
    val Hand: ImageVector          = Lucide.Hand
    val Check: ImageVector         = Lucide.Check
    val Archive: ImageVector       = Lucide.Archive
    val Trash: ImageVector         = Lucide.Trash2
    val X: ImageVector             = Lucide.X
    val ChevronLeft: ImageVector   = Lucide.ChevronLeft
    val ChevronRight: ImageVector  = Lucide.ChevronRight
    val User: ImageVector          = Lucide.User
    val Layers: ImageVector        = Lucide.Layers
    val Download: ImageVector      = Lucide.Download
    val Settings: ImageVector      = Lucide.Settings

    // ── App chrome ───────────────────────────────────────────────────────────
    val Add: ImageVector      = Lucide.CirclePlus
    val BarChart: ImageVector = Lucide.ChartBar
    val Calendar: ImageVector = Lucide.Calendar

    // ── Fallback ─────────────────────────────────────────────────────────────
    val Other: ImageVector = Lucide.Tag

    // ─────────────────────────────────────────────────────────────────────────
    // Key → icon mapping.
    // The database stores the key string; the UI calls fromKey() to render it.
    // ─────────────────────────────────────────────────────────────────────────

    private val map: Map<String, ImageVector> = mapOf(
        "wallet"         to Wallet,
        "banknote"       to Banknote,
        "credit_card"    to CreditCard,
        "piggy_bank"     to PiggyBank,
        "trending_up"    to TrendingUp,
        "receipt"        to Receipt,
        "shopping_cart"  to ShoppingCart,
        "shopping_bag"   to ShoppingBag,
        "store"          to Store,
        "utensils"       to Utensils,
        "coffee"         to Coffee,
        "car"            to Car,
        "bus"            to Bus,
        "plane"          to Plane,
        "heart"          to Heart,
        "dumbbell"       to Dumbbell,
        "sparkles"       to Sparkles,
        "home"           to Home,
        "zap"            to Zap,
        "droplets"       to Droplets,
        "wifi"           to Wifi,
        "briefcase"      to Briefcase,
        "laptop"         to Laptop,
        "graduation_cap" to GraduationCap,
        "clapperboard"   to Clapperboard,
        "music"          to Music,
        "target"         to Target,
        "add"            to Add,
        "bar_chart"      to BarChart,
        "calendar"       to Calendar,
        "other"          to Other,
    )

    fun fromKey(key: String): ImageVector = map[key] ?: Other

    // All icons available in the category / goal icon picker
    val pickerIcons: List<Pair<String, ImageVector>> = map.entries
        .filterNot { it.key in setOf("add", "bar_chart", "calendar") }
        .map { it.key to it.value }
}