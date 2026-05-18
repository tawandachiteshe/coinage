package com.tawandachiteshe.coinage.feature.scan

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

object ReceiptParser {

    fun parse(rawText: String, emphasizedLineTexts: Set<String> = emptySet()): ScannedReceipt {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        return ScannedReceipt(
            merchant = extractMerchant(lines, emphasizedLineTexts),
            amount = extractAmount(lines),
            date = extractDate(rawText),
            suggestedCategoryId = suggestCategory(rawText),
            rawText = rawText,
        )
    }

    // ─── Tuning constants ────────────────────────────────────────────────────

    private val  MERCHANT_LENGTH_RANGE  = 2..60
    private const val MERCHANT_SCAN_WINDOW    = 10

    private const val SCORE_PER_EARLY_LINE    = 3
    private const val SCORE_ALL_CAPS          = 20
    private const val SCORE_EMPHASIZED        = 30
    private const val SCORE_KNOWN_BRAND       = 35
    private const val SCORE_LETTER_RATIO_HIGH = 15
    private const val SCORE_LETTER_RATIO_MED  = 8
    private const val SCORE_WORD_COUNT_OK     = 5
    private const val SCORE_DIGIT_PENALTY     = 12
    private const val LETTER_RATIO_HIGH       = 0.85f
    private const val LETTER_RATIO_MED        = 0.70f
    private val       BRAND_WORD_COUNT        = 1..4

    private val       LOCATION_DESC_LENGTH    = 2..25

    private const val SCORE_TOTAL_HIGH_CONF   = 100
    private const val SCORE_TOTAL_MED_CONF    = 50
    private const val POS_LOWER_HALF          = 0.50f
    private const val POS_LOWER_THIRD         = 0.65f
    private const val POS_BOTTOM_FIFTH        = 0.80f
    private const val SCORE_POS_LOWER_HALF    = 15
    private const val SCORE_POS_LOWER_THIRD   = 15
    private const val SCORE_POS_BOTTOM_FIFTH  = 10

    private const val MIN_YEAR               = 1900

    private const val PHONE_MIN_DIGITS       = 7
    private const val PHONE_DIGIT_RATIO      = 0.5f

    // ─── Merchant ────────────────────────────────────────────────────────────

    private val SKIP_WORDS = setOf(
        "tel", "phone", "fax", "www", "http",
        "reg", "vat", "tax", "receipt", "invoice", "cash", "sale",
        "change", "card", "visa", "mastercard",
        "thank", "welcome", "duplicate", "retain",
    )

    private val SKIP_PHRASES = listOf(
        "thank you", "please retain", "keep this",
        "customer copy", "merchant copy",
        "serving you", "shop at", "store hours",
        "have a nice",
    )

    private val ADDRESS_WORDS = setOf(
        "street", "st", "ave", "avenue", "road", "rd", "drive", "dr",
        "floor", "suite", "unit", "box", "mall", "park", "plaza", "corner", "cnr",
    )

    private val LEGAL_WORDS = setOf(
        "pty", "ltd", "llc", "inc", "corp", "cc", "sa", "plc", "gmbh", "bv", "nv",
    )

    private val BRAND_SEPARATORS = listOf(" - ", " – ", " | ", " / ")

    private val TRADING_AS_MARKERS = listOf(" t/a ", " ta ", " trading as ")

    private fun isMerchantCandidate(line: String): Boolean {
        if (line.length !in MERCHANT_LENGTH_RANGE) return false
        if (looksLikePhone(line)) return false
        if (looksLikeDate(line)) return false
        if (amountsIn(line).isNotEmpty()) return false
        if (line.startsWith("#")) return false
        if (line.all { it.isDigit() || it == '-' || it == '/' || it.isWhitespace() }) return false
        val lower = line.lowercase()
        val words = wordsOf(lower)
        if (words.any { it in SKIP_WORDS }) return false
        if (SKIP_PHRASES.any { lower.contains(it) }) return false
        if (words.any { it in ADDRESS_WORDS }) return false
        return true
    }

    private fun scoreMerchant(line: String, position: Int, emphasizedLineTexts: Set<String>): Int {
        var score = 0
        val lower = line.lowercase()
        score += (MERCHANT_SCAN_WINDOW - position).coerceAtLeast(0) * SCORE_PER_EARLY_LINE
        if (line == line.uppercase() && line.any { it.isLetter() }) score += SCORE_ALL_CAPS
        if (line.trim() in emphasizedLineTexts) score += SCORE_EMPHASIZED
        if (CATEGORY_KEYWORDS.values.flatten().any { lower.contains(it) }) score += SCORE_KNOWN_BRAND
        val letterRatio = line.count { it.isLetter() || it.isWhitespace() }.toFloat() / line.length
        when {
            letterRatio >= LETTER_RATIO_HIGH -> score += SCORE_LETTER_RATIO_HIGH
            letterRatio >= LETTER_RATIO_MED  -> score += SCORE_LETTER_RATIO_MED
        }
        if (line.any { it.isDigit() }) score -= SCORE_DIGIT_PENALTY
        if (wordsOf(line).size in BRAND_WORD_COUNT) score += SCORE_WORD_COUNT_OK
        return score
    }

    private fun extractMerchant(lines: List<String>, emphasizedLineTexts: Set<String> = emptySet()): String? {
        data class Candidate(val line: String, val score: Int)

        val best = lines.take(MERCHANT_SCAN_WINDOW)
            .mapIndexedNotNull { idx, line ->
                if (isMerchantCandidate(line))
                    Candidate(line, scoreMerchant(line, idx, emphasizedLineTexts))
                else null
            }
            .maxByOrNull { it.score }
            ?.line ?: return null

        return best
            .let(::splitOnTradingAs)
            .normalizeSpaces()
            .let(::stripLegalSuffix)
            .let(::stripStoreCode)
            .let(::stripLocationSuffix)
            .trim()
            .ifBlank { null }
    }

    private fun splitOnTradingAs(name: String): String {
        val lower = name.lowercase()
        for (marker in TRADING_AS_MARKERS) {
            val idx = lower.indexOf(marker)
            if (idx >= 0) return name.substring(idx + marker.length).trim()
        }
        return name
    }

    private fun stripLegalSuffix(name: String): String {
        val words = name.trim().split(' ').filter { it.isNotBlank() }
        var end = words.size
        while (end > 0 && words[end - 1].lowercase().trimEnd('.', ',', '(', ')') in LEGAL_WORDS) end--
        return words.take(end).joinToString(" ").trim()
    }

    private fun stripStoreCode(name: String): String {
        var result = name.trimEnd()
        while (result.isNotEmpty()) {
            val sepIdx = result.indexOfLast { it == '-' || it == '#' || it == '–' }
            if (sepIdx < 0) break
            val after = result.substring(sepIdx + 1).trim()
            if (after.isNotEmpty() && after.all { it.isDigit() || it.isWhitespace() }) {
                result = result.substring(0, sepIdx).trimEnd()
            } else break
        }
        return result
    }

    private fun stripLocationSuffix(name: String): String {
        for (sep in BRAND_SEPARATORS) {
            val idx = name.indexOf(sep)
            if (idx <= 0) continue
            val after = name.substring(idx + sep.length).trim()
            if (after.length in LOCATION_DESC_LENGTH && after.first().isLetter()) return name.substring(0, idx).trim()
        }
        return name
    }

    // ─── Amount ──────────────────────────────────────────────────────────────

    private val SKIP_AMOUNT_LINES = listOf(
        "cash", "change", "tendered", "card", "visa", "mastercard",
        "discount", "saving", "points", "loyalty",
        "vat excl", "tax excl",
        "qty", "price each", "per unit",
    )

    private val TOTAL_HIGH = listOf(
        "total", "grand total", "total due", "total amount due", "total amount",
        "amount due", "amount to pay", "please pay", "balance due", "net payable",
        "total incl vat", "total incl", "total to pay", "net amount",
        "you owe",
    )

    private val TOTAL_MED = listOf("amount", "balance", "net")

    // Amounts preceded by a currency symbol; R requires a word boundary (not mid-word).
    // Allows space-grouped thousands: "R 8 999,00".
    private val CURRENCY_AMOUNT_RE = Regex("""(?:[$£€¥₹]|(?<![A-Za-z])R)\s*(\d[\d ,]*(?:[.,]\d{1,2})?)""")

    // Bare decimal amounts without a currency prefix (no space grouping, avoids false positives).
    private val PLAIN_AMOUNT_RE = Regex("""(?<![,.\d])(\d[\d,]*[.,]\d{1,2})(?![,.\d])""")

    // Whole-number amounts without a currency prefix or decimal ("TOTAL   144").
    // Minimum 2 digits to avoid single-digit quantity false positives.
    private val PLAIN_WHOLE_RE = Regex("""(?<![,.\d])(\d{2,})(?![,.\d])""")

    private data class AmountCandidate(val amount: Double, val score: Int)

    private fun extractAmount(lines: List<String>): Double? {
        val total = lines.size
        val candidates = mutableListOf<AmountCandidate>()

        lines.forEachIndexed { idx, line ->
            val lower = line.lowercase()
            // Collapse runs of whitespace so "TOTAL   AMOUNT" matches the phrase "total amount".
            val normalized = lower.replace(Regex("\\s+"), " ")
            val isHighConfidence = TOTAL_HIGH.any { normalized.contains(it) }
            if (!isHighConfidence && SKIP_AMOUNT_LINES.any { lower.contains(it) }) return@forEachIndexed

            val amounts = amountsIn(line).filter { it > 0.0 }
            if (amounts.isEmpty()) return@forEachIndexed

            val lineAmount = amounts.max()

            var score = 0
            when {
                TOTAL_HIGH.any { normalized.contains(it) } -> score += SCORE_TOTAL_HIGH_CONF
                TOTAL_MED.any { normalized.contains(it) } -> score += SCORE_TOTAL_MED_CONF
            }
            val posRatio = idx.toFloat() / total.coerceAtLeast(1)
            if (posRatio > POS_LOWER_HALF)   score += SCORE_POS_LOWER_HALF
            if (posRatio > POS_LOWER_THIRD)  score += SCORE_POS_LOWER_THIRD
            if (posRatio > POS_BOTTOM_FIFTH) score += SCORE_POS_BOTTOM_FIFTH

            candidates.add(AmountCandidate(lineAmount, score))
        }

        if (candidates.isEmpty()) return null
        val maxScore = candidates.maxOf { it.score }
        return candidates.filter { it.score == maxScore }.maxOfOrNull { it.amount }
    }

    private fun amountsIn(line: String): List<Double> {
        val found = mutableListOf<Double>()
        // Each pass blanks its matches so later passes don't double-count sub-spans.
        var remaining = line
        CURRENCY_AMOUNT_RE.findAll(line).forEach { m ->
            normalizeAmount(m.groupValues[1].trimEnd(' ', ',', '.'))
                ?.takeIf { it > 0 }?.let { found.add(it) }
            remaining = remaining.replaceRange(m.range, " ".repeat(m.value.length))
        }
        PLAIN_AMOUNT_RE.findAll(remaining).forEach { m ->
            normalizeAmount(m.groupValues[1])?.takeIf { it > 0 }?.let { found.add(it) }
            remaining = remaining.replaceRange(m.range, " ".repeat(m.value.length))
        }
        // Whole-number fallback: "TOTAL   144" — runs only on whatever spans remain.
        PLAIN_WHOLE_RE.findAll(remaining).forEach { m ->
            normalizeAmount(m.groupValues[1])?.takeIf { it > 0 }?.let { found.add(it) }
        }
        return found.distinct()
    }

    // Handles US (1,234.56), EU (1.234,56), and SA space-grouped (1 234,56) formats.
    private fun normalizeAmount(raw: String): Double? {
        val cleaned = raw.replace(" ", "")
        return when {
            cleaned.contains(',') && cleaned.contains('.') -> {
                val lastComma = cleaned.lastIndexOf(',')
                val lastDot   = cleaned.lastIndexOf('.')
                if (lastDot > lastComma) cleaned.replace(",", "")
                else cleaned.replace(".", "").replace(",", ".")
            }
            cleaned.contains(',') -> {
                val afterComma = cleaned.substringAfterLast(',')
                if (afterComma.length <= 2) cleaned.replace(",", ".")
                else cleaned.replace(",", "")
            }
            else -> cleaned
        }.toDoubleOrNull()
    }

    // ─── Date ────────────────────────────────────────────────────────────────

    private val MONTH_MAP = mapOf(
        "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "may" to 5, "jun" to 6,
        "jul" to 7, "aug" to 8, "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12,
    )

    // YYYY-MM-DD or YYYY/MM/DD — backreference \2 enforces consistent separator
    private val DATE_YMD_RE = Regex("""(\d{4})([-/])(\d{1,2})\2(\d{1,2})""")
    // DD-MM-YYYY or DD/MM/YYYY — backreference \2 enforces consistent separator
    private val DATE_DMY_RE = Regex("""(\d{1,2})([-/])(\d{1,2})\2(\d{4})""")
    // "15 Jan 2024"
    private val DATE_DMY_TEXT_RE = Regex("""(\d{1,2})\s+([A-Za-z]{3,9})\s+(\d{4})""", RegexOption.IGNORE_CASE)
    // "Jan 15, 2024" or "Jan 15 2024"
    private val DATE_MDY_TEXT_RE = Regex("""([A-Za-z]{3,9})\s+(\d{1,2})[,\s]+(\d{4})""", RegexOption.IGNORE_CASE)

    private fun looksLikeDate(line: String): Boolean =
        parseNumericDate(line) != null || parseTextDate(line) != null

    private fun extractDate(rawText: String): Long? {
        for (line in rawText.lines()) {
            (parseNumericDate(line) ?: parseTextDate(line))?.let { return it }
        }
        return null
    }

    private fun parseNumericDate(line: String): Long? {
        DATE_YMD_RE.find(line)?.let { m ->
            tryDate(m.groupValues[1].toInt(), m.groupValues[3].toInt(), m.groupValues[4].toInt())
                ?.let { return it }
        }
        DATE_DMY_RE.find(line)?.let { m ->
            tryDate(m.groupValues[4].toInt(), m.groupValues[3].toInt(), m.groupValues[1].toInt())
                ?.let { return it }
        }
        return null
    }

    private fun parseTextDate(line: String): Long? {
        DATE_DMY_TEXT_RE.find(line)?.let { m ->
            val month = MONTH_MAP[m.groupValues[2].take(3).lowercase()] ?: return@let
            tryDate(m.groupValues[3].toInt(), month, m.groupValues[1].toInt())?.let { return it }
        }
        DATE_MDY_TEXT_RE.find(line)?.let { m ->
            val month = MONTH_MAP[m.groupValues[1].take(3).lowercase()] ?: return@let
            tryDate(m.groupValues[3].toInt(), month, m.groupValues[2].toInt())?.let { return it }
        }
        return null
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun tryDate(year: Int, month: Int, day: Int): Long? = try {
        LocalDate(year, month, day).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    } catch (_: Exception) { null }

    // ─── Category ────────────────────────────────────────────────────────────

    private val CATEGORY_KEYWORDS: Map<String, List<String>> = mapOf(
        "cat_groceries"     to listOf(
            "grocery", "groceries", "supermarket", "food mart",
            "woolworths", "checkers", "spar", "pick n pay", "picknpay",
            "shoprite", "food lover", "freshstop",
            "walmart", "costco", "whole foods", "aldi", "kroger", "trader joe",
        ),
        "cat_dining"        to listOf(
            "restaurant", "cafe", "coffee", "mcdonald", "burger king",
            "pizza", "kfc", "starbucks", "subway", "nando", "steers",
            "fishaways", "wimpy", "spur", "ocean basket",
            "bistro", "grill", "kitchen", "takeaway", "takeout", "diner",
        ),
        "cat_transport"     to listOf(
            "uber", "lyft", "bolt", "taxi", "petrol", "diesel", "fuel",
            "gas station", "bp ", "shell", "engen", "caltex", "sasol",
            "total energies", "astron", "parking", "toll",
        ),
        "cat_health"        to listOf(
            "pharmacy", "chemist", "hospital", "clinic", "doctor",
            "medical", "dental", "dischem", "clicks", "medirite", "alpha pharm",
        ),
        "cat_utilities"     to listOf(
            "electricity", "water bill", "internet", "fibre", "wifi",
            "airtime", "data bundle",
            "eskom", "vodacom", "mtn", "telkom", "rain", "cell c", "afrihost",
        ),
        "cat_entertainment" to listOf(
            "cinema", "ster-kinekor", "nu metro", "movies", "event",
            "netflix", "spotify", "showmax", "dstv", "game",
        ),
        "cat_shopping"      to listOf(
            "amazon", "takealot", "zara", "h&m", "mr price", "truworths",
            "edgars", "foschini", "clothing", "fashion", "sport",
            "hardware", "builder", "mica", "leroy merlin",
            "furniture", "makro", "game store",
        ),
        "cat_housing"       to listOf("rent", "mortgage", "lease", "property", "body corporate"),
    )

    private fun suggestCategory(rawText: String): String? {
        val lower = rawText.lowercase()
        return CATEGORY_KEYWORDS.entries.firstOrNull { (_, kws) -> kws.any { lower.contains(it) } }?.key
    }

    // ─── String utilities ────────────────────────────────────────────────────

    private fun wordsOf(text: String): List<String> =
        text.split(' ', '\t', ',', '.', ':', ';')
            .map { it.trim().trimEnd('.', ',', '(', ')') }
            .filter { it.isNotBlank() }

    private fun String.normalizeSpaces(): String =
        split(' ').filter { it.isNotBlank() }.joinToString(" ")

    private fun looksLikePhone(line: String): Boolean {
        val digits = line.count { it.isDigit() }
        return digits >= PHONE_MIN_DIGITS && digits.toFloat() / line.length > PHONE_DIGIT_RATIO
    }
}