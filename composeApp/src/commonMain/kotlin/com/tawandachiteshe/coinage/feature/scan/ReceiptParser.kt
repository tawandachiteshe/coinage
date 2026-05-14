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

    // ─── Merchant ────────────────────────────────────────────────────────────

    private val PHONE_REGEX = Regex("""[+\d][\d\s()\-]{6,}""")
    private val SKIP_MERCHANT = listOf(
        "tel:", "tel :", "phone:", "fax:", "www.", "http", "@",
        "reg no", "vat no", "tax no", "reg:", "vat:", "co. reg",
        "receipt", "invoice", "tax invoice", "cash sale",
        "change", "cash", "card", "visa", "mastercard",
        // taglines / pleasantries
        "thank you", "welcome", "have a nice", "please retain",
        "keep this", "customer copy", "merchant copy", "duplicate",
        "serving you", "shop at", "store hours",
    )
    private val ADDRESS_WORDS = listOf(
        "street", "st.", " ave", "avenue", "road", "rd", "drive", "dr.",
        "floor", "suite", "unit", "po box", "p.o box", "shopping centre",
        "mall", "park", "plaza", "corner", "cnr",
    )

    // Legal suffixes to strip from the extracted merchant name
    private val LEGAL_SUFFIX = Regex(
        """\s*[(\[]?(?:pty\.?\s*ltd|ltd|llc|inc|corp|cc|sa|plc|gmbh|bv|nv|co\.?\s*ltd)[)\]]?\.?\s*$""",
        RegexOption.IGNORE_CASE,
    )
    // Store/branch codes appended to names: "WOOLWORTHS #123", "KFC - 045"
    private val STORE_CODE = Regex("""[\s\-–#]+\d+\s*$""")
    // Location/branch descriptor after a separator: "PICK N PAY - SANDTON", "SPAR | RIVONIA"
    private val LOCATION_SUFFIX = Regex("""\s*[-–|/]\s*[A-Za-z][A-Za-z\s]{2,25}$""")
    // "T/A" or "TRADING AS" — the real brand name follows
    private val TRADING_AS = Regex("""\bt/?a\b|\btrading\s+as\b""", RegexOption.IGNORE_CASE)

    private fun isMerchantCandidate(line: String): Boolean {
        val lower = line.lowercase()
        return line.length in 2..60 &&
            !PHONE_REGEX.containsMatchIn(line) &&
            !looksLikeDate(line) &&
            !AMOUNT_REGEX.containsMatchIn(line) &&
            !line.startsWith("#") &&
            SKIP_MERCHANT.none { lower.contains(it) } &&
            ADDRESS_WORDS.none { lower.contains(it) } &&
            !line.all { c -> c.isDigit() || c == '-' || c == '/' || c.isWhitespace() }
    }

    private fun scoreMerchant(line: String, position: Int, emphasizedLineTexts: Set<String>): Int {
        var score = 0
        val lower = line.lowercase()
        // Earlier lines are more likely to be the merchant header
        score += (10 - position).coerceAtLeast(0) * 3
        // ALL CAPS = typical receipt header style
        if (line == line.uppercase() && line.any { it.isLetter() }) score += 20
        // Bold/large text detected by OCR bounding-box height
        if (line.trim() in emphasizedLineTexts) score += 30
        // Known brand name from our category keywords — strong signal
        if (CATEGORY_KEYWORDS.values.flatten().any { lower.contains(it) }) score += 35
        // Mostly letters/spaces = clean brand name
        val letterRatio = line.count { it.isLetter() || it.isWhitespace() }.toFloat() / line.length
        if (letterRatio >= 0.85f) score += 15
        else if (letterRatio >= 0.70f) score += 8
        // Embedded digits (not a pure digit line) suggest a store code or ref number
        if (line.any { it.isDigit() }) score -= 12
        // Sweet-spot word count: 1–4 words is typical for a brand
        val wordCount = line.trim().split(Regex("""\s+""")).size
        if (wordCount in 1..4) score += 5
        return score
    }

    private fun extractMerchant(lines: List<String>, emphasizedLineTexts: Set<String> = emptySet()): String? {
        data class Candidate(val line: String, val score: Int)

        val best = lines.take(10)
            .mapIndexedNotNull { idx, line ->
                if (isMerchantCandidate(line)) Candidate(line, scoreMerchant(line, idx, emphasizedLineTexts)) else null
            }
            .maxByOrNull { it.score }
            ?.line ?: return null

        // "JOHN'S STORE T/A WOOLWORTHS" → "WOOLWORTHS"
        val afterTa = if (TRADING_AS.containsMatchIn(best))
            TRADING_AS.split(best).last().trim()
        else best

        return afterTa
            .trim()
            .replace(Regex("""\s{2,}"""), " ")
            .replace(LEGAL_SUFFIX, "")
            .replace(STORE_CODE, "")
            .replace(LOCATION_SUFFIX, "")
            .trim()
            .ifBlank { null }
    }

    // ─── Amount ──────────────────────────────────────────────────────────────

    // Alt 1 (tried first — more specific): space-grouped thousands, currency symbol required.
    //   Handles "R 8 999,00" common on SA receipts. Currency required to avoid false
    //   positives on phone numbers like "011 555 1234".
    // Alt 2: standard dot/comma separators, currency symbol optional.
    private val AMOUNT_REGEX = Regex(
        """[${'$'}R£€¥₹]\s*(\d{1,3}(?:\s\d{3})+(?:[.,]\d{1,2})?)(?!\d)""" +
        """|[${'$'}R£€¥₹]?\s*(\d{1,6}(?:[.,]\d{3})*(?:[.,]\d{1,2})?)(?!\d)"""
    )

    // Lines whose amount we should never treat as the receipt total.
    // NOTE: high-confidence total lines (TOTAL_HIGH) are never skipped even if they
    // contain these words (e.g. "TOTAL INCL VAT" contains "vat").
    private val SKIP_AMOUNT_LINES = listOf(
        "cash", "change", "tendered", "card", "visa", "mastercard",
        "discount", "saving", "points", "loyalty",
        "vat excl", "tax excl",          // standalone VAT/tax lines, not "total incl vat"
        "qty", "price each", "per unit",
    )

    // High-confidence total keywords (score 100)
    private val TOTAL_HIGH = listOf(
        "grand total", "total due", "total amount due", "amount due",
        "amount to pay", "please pay", "balance due", "net payable",
        "total incl vat", "total incl", "total to pay", "net amount",
        "you owe",
    )
    // Medium-confidence total keywords (score 50)
    private val TOTAL_MED = listOf(
        "total", "amount", "balance", "net",
    )

    private data class AmountCandidate(val amount: Double, val score: Int)

    private fun extractAmount(lines: List<String>): Double? {
        val total = lines.size
        val candidates = mutableListOf<AmountCandidate>()

        lines.forEachIndexed { idx, line ->
            val lower = line.lowercase()
            val isHighConfidence = TOTAL_HIGH.any { lower.contains(it) }
            // TOTAL_HIGH lines are never skipped — "TOTAL INCL VAT" contains "vat" but is the real total.
            if (!isHighConfidence && SKIP_AMOUNT_LINES.any { lower.contains(it) }) return@forEachIndexed

            val amounts = AMOUNT_REGEX.findAll(line)
                .mapNotNull { m ->
                    // groupValues[1] = alt-1 (standard), groupValues[2] = alt-2 (space-grouped)
                    normalizeAmount(m.groupValues[1].ifEmpty { m.groupValues[2] })
                }
                .filter { it > 0.0 }
                .toList()
            if (amounts.isEmpty()) return@forEachIndexed

            val lineAmount = amounts.maxOrNull() ?: return@forEachIndexed

            var score = 0
            when {
                TOTAL_HIGH.any { lower.contains(it) } -> score += 100
                TOTAL_MED.any { lower.contains(it) } -> score += 50
            }
            // Receipts print the grand total in the lower half
            val posRatio = idx.toFloat() / total.coerceAtLeast(1)
            if (posRatio > 0.50f) score += 15
            if (posRatio > 0.65f) score += 15
            if (posRatio > 0.80f) score += 10

            candidates.add(AmountCandidate(lineAmount, score))
        }

        if (candidates.isEmpty()) return null

        val maxScore = candidates.maxOf { it.score }
        // Among highest-scored candidates, pick the largest amount
        return candidates
            .filter { it.score == maxScore }
            .maxOfOrNull { it.amount }
    }

    // Normalise amount strings that may use either convention:
    //   US  : 1,234.56  →  1234.56
    //   EU  : 1.234,56  →  1234.56
    //   ZA  : 1 234,56  →  1234.56  (space-grouped)
    private fun normalizeAmount(raw: String): Double? {
        val cleaned = raw.replace(" ", "")
        return when {
            cleaned.contains(',') && cleaned.contains('.') -> {
                val lastComma = cleaned.lastIndexOf(',')
                val lastDot = cleaned.lastIndexOf('.')
                if (lastDot > lastComma) cleaned.replace(",", "")          // US
                else cleaned.replace(".", "").replace(",", ".")             // EU
            }
            cleaned.contains(',') -> {
                val afterComma = cleaned.substringAfterLast(',')
                if (afterComma.length <= 2) cleaned.replace(",", ".")      // EU decimal
                else cleaned.replace(",", "")                              // thousands
            }
            else -> cleaned
        }.toDoubleOrNull()
    }

    // ─── Date ────────────────────────────────────────────────────────────────

    private val DATE_REGEXES = listOf(
        Regex("""\b(\d{4})[/-](\d{2})[/-](\d{2})\b"""),                          // YYYY-MM-DD
        Regex("""\b(\d{4})/(\d{2})/(\d{2})\b"""),                                // YYYY/MM/DD
        Regex("""\b(\d{1,2})[/-](\d{1,2})[/-](\d{4})\b"""),                     // DD/MM/YYYY or MM/DD/YYYY
        Regex("""\b(\d{1,2})\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*[\s,]+(\d{2,4})\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*[\s.]+(\d{1,2})[\s,]+(\d{4})\b""", RegexOption.IGNORE_CASE),
    )

    private fun looksLikeDate(line: String) = DATE_REGEXES.any { it.containsMatchIn(line) }

    private val MONTH_MAP = mapOf(
        "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "may" to 5, "jun" to 6,
        "jul" to 7, "aug" to 8, "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12,
    )

    private fun extractDate(rawText: String): Long? {
        // ISO / YYYY-MM-DD / YYYY/MM/DD
        Regex("""\b(\d{4})[/-](\d{1,2})[/-](\d{1,2})\b""").find(rawText)?.let { m ->
            return tryParseDate(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())
        }
        // DD/MM/YYYY — common on receipts
        Regex("""\b(\d{1,2})[/-](\d{1,2})[/-](\d{4})\b""").find(rawText)?.let { m ->
            return tryParseDate(m.groupValues[3].toInt(), m.groupValues[2].toInt(), m.groupValues[1].toInt())
        }
        // "15 Jan 2024" or "Jan 15, 2024"
        Regex("""\b(\d{1,2})\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*[\s,]+(\d{4})\b""", RegexOption.IGNORE_CASE).find(rawText)?.let { m ->
            val month = MONTH_MAP[m.groupValues[2].lowercase().take(3)] ?: return@let
            return tryParseDate(m.groupValues[3].toInt(), month, m.groupValues[1].toInt())
        }
        Regex("""\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*[\s.]+(\d{1,2})[\s,]+(\d{4})\b""", RegexOption.IGNORE_CASE).find(rawText)?.let { m ->
            val month = MONTH_MAP[m.groupValues[1].lowercase().take(3)] ?: return@let
            return tryParseDate(m.groupValues[3].toInt(), month, m.groupValues[2].toInt())
        }
        return null
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun tryParseDate(year: Int, month: Int, day: Int): Long? = try {
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
}