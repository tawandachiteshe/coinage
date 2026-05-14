package com.tawandachiteshe.coinage.feature.scan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReceiptParserTest {

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun parse(text: String) = ReceiptParser.parse(text.trimIndent())

    // ── Amount extraction ─────────────────────────────────────────────────────

    @Test
    fun `extracts ZAR total incl vat over other amounts`() {
        val receipt = """
            WOOLWORTHS FOOD
            MILK 2L           R25,99
            BREAD             R18,99
            SUBTOTAL          R44,98
            DISCOUNT          R-5,00
            TOTAL INCL VAT    R39,98
            CASH              R50,00
            CHANGE            R10,02
        """
        assertEquals(39.98, parse(receipt).amount)
    }

    @Test
    fun `extracts grand total keyword`() {
        val receipt = """
            KFC SANDTON
            ZINGER BURGER     R89,90
            CHIPS             R29,90
            GRAND TOTAL       R119,80
            CARD PAYMENT      R119,80
        """
        assertEquals(119.80, parse(receipt).amount)
    }

    @Test
    fun `skips cash and change lines`() {
        val receipt = """
            SPAR
            ITEMS TOTAL       R55,00
            CASH TENDERED     R100,00
            CHANGE            R45,00
        """
        // Should pick ITEMS TOTAL, not cash or change
        assertEquals(55.00, parse(receipt).amount)
    }

    @Test
    fun `handles US dot-decimal format`() {
        val receipt = """
            STARBUCKS
            LATTE             $5.50
            MUFFIN            $3.25
            TOTAL DUE         $8.75
        """
        assertEquals(8.75, parse(receipt).amount)
    }

    @Test
    fun `handles EU comma-decimal format`() {
        val receipt = """
            EDEKA
            BROT              1,99
            KAFFEE            3,49
            GESAMT            5,48
            TOTAL             5,48
        """
        assertEquals(5.48, parse(receipt).amount)
    }

    @Test
    fun `handles thousands-grouped ZAR amount`() {
        val receipt = """
            MAKRO
            TELEVISION        R 8 999,00
            TOTAL INCL VAT    R 8 999,00
        """
        assertEquals(8999.00, parse(receipt).amount)
    }

    @Test
    fun `position scoring favours bottom half when no keywords present`() {
        val receipt = """
            PRODUCT A         R10,00
            PRODUCT B         R20,00
            PRODUCT C         R30,00
            PRODUCT D         R40,00
            PRODUCT E         R50,00
            PRODUCT F         R60,00
        """
        // No total keyword — bottom-half lines score higher; R60 is last
        assertEquals(60.0, parse(receipt).amount)
    }

    @Test
    fun `returns null amount when no numbers found`() {
        val receipt = "THANK YOU FOR SHOPPING\nHAVE A NICE DAY"
        assertNull(parse(receipt).amount)
    }

    // ── Merchant extraction ───────────────────────────────────────────────────

    @Test
    fun `prefers known brand over first line`() {
        val receipt = """
            TAX INVOICE
            WOOLWORTHS FOOD
            Sandton City
            Tel: 011 123 4567
            TOTAL R55,00
        """
        assertEquals("WOOLWORTHS FOOD", parse(receipt).merchant)
    }

    @Test
    fun `extracts all-caps brand from top lines`() {
        val receipt = """
            PICK N PAY
            ROSEBANK MALL
            Tel: 011 555 0000
            TOTAL R120,00
        """
        assertEquals("PICK N PAY", parse(receipt).merchant)
    }

    @Test
    fun `splits on trading-as pattern and takes the brand after it`() {
        val receipt = """
            JOHN'S STORE T/A SPAR
            123 Main Street
            TOTAL R33,00
        """
        assertEquals("SPAR", parse(receipt).merchant)
    }

    @Test
    fun `strips legal suffix`() {
        val receipt = """
            FRESHSTOP PTY LTD
            BP FORECOURT
            TOTAL R80,00
        """
        assertEquals("FRESHSTOP", parse(receipt).merchant)
    }

    @Test
    fun `strips location suffix after dash`() {
        val receipt = """
            CHECKERS - RIVONIA
            Tel: 011 807 1234
            TOTAL R200,00
        """
        assertEquals("CHECKERS", parse(receipt).merchant)
    }

    @Test
    fun `skips phone numbers`() {
        val receipt = """
            011 555 1234
            NANDO'S
            TOTAL R150,00
        """
        assertEquals("NANDO'S", parse(receipt).merchant)
    }

    @Test
    fun `skips address lines`() {
        val receipt = """
            15 Main Street Sandton
            STEERS
            TOTAL R89,00
        """
        assertEquals("STEERS", parse(receipt).merchant)
    }

    @Test
    fun `skips receipt header words`() {
        val receipt = """
            TAX INVOICE
            CUSTOMER COPY
            DISCHEM
            TOTAL R45,00
        """
        assertEquals("DISCHEM", parse(receipt).merchant)
    }

    // ── Date extraction ───────────────────────────────────────────────────────

    @Test
    fun `extracts ISO date`() {
        val receipt = "WOOLWORTHS\nDate: 2024-05-15\nTOTAL R100,00"
        assertNotNull(parse(receipt).date)
    }

    @Test
    fun `extracts DD MM YYYY separated by slashes`() {
        val receipt = "CHECKERS\n15/05/2024\nTOTAL R100,00"
        assertNotNull(parse(receipt).date)
    }

    @Test
    fun `extracts text month format`() {
        val receipt = "SPAR\n15 Jan 2024\nTOTAL R100,00"
        assertNotNull(parse(receipt).date)
    }

    @Test
    fun `extracts month-first text format`() {
        val receipt = "STARBUCKS\nJan 15, 2024\nTOTAL $8.75"
        assertNotNull(parse(receipt).date)
    }

    @Test
    fun `returns null date when none found`() {
        val receipt = "WOOLWORTHS\nTOTAL R100,00"
        assertNull(parse(receipt).date)
    }

    @Test
    fun `rejects invalid date`() {
        val receipt = "SPAR\n99/99/9999\nTOTAL R50,00"
        assertNull(parse(receipt).date)
    }

    // ── Category suggestion ───────────────────────────────────────────────────

    @Test
    fun `suggests groceries for woolworths`() {
        assertEquals("cat_groceries", parse("WOOLWORTHS FOOD\nTOTAL R100,00").suggestedCategoryId)
    }

    @Test
    fun `suggests dining for kfc`() {
        assertEquals("cat_dining", parse("KFC\nTOTAL R89,00").suggestedCategoryId)
    }

    @Test
    fun `suggests transport for petrol`() {
        assertEquals("cat_transport", parse("ENGEN\nPETROL 95\nTOTAL R650,00").suggestedCategoryId)
    }

    @Test
    fun `suggests health for pharmacy`() {
        assertEquals("cat_health", parse("DISCHEM PHARMACY\nTOTAL R45,00").suggestedCategoryId)
    }

    @Test
    fun `returns null category for unknown merchant`() {
        assertNull(parse("ACME CORP\nTOTAL R999,00").suggestedCategoryId)
    }

    // ── Full receipt integration ──────────────────────────────────────────────

    @Test
    fun `full SA receipt parses all fields`() {
        val receipt = """
            CHECKERS HYPER
            ROSEBANK MALL, JOHANNESBURG
            VAT No: 4123456789
            Date: 2024-03-10

            APPLES 1KG        R 24,99
            CHICKEN BREAST    R 89,99
            BREAD             R 18,99

            SUBTOTAL          R133,97
            TOTAL INCL VAT    R133,97
            CASH              R150,00
            CHANGE            R 16,03
        """
        val result = parse(receipt)
        assertEquals("CHECKERS HYPER", result.merchant)
        assertEquals(133.97, result.amount)
        assertNotNull(result.date)
        assertEquals("cat_groceries", result.suggestedCategoryId)
    }

    @Test
    fun `full US receipt parses all fields`() {
        val receipt = """
            STARBUCKS
            123 5th Avenue, New York
            Tel: 212-555-0100
            Jan 15, 2024

            CARAMEL LATTE     $6.75
            BLUEBERRY MUFFIN  $3.95

            SUBTOTAL          $10.70
            TAX               $0.96
            TOTAL DUE         $11.66
            VISA *1234        $11.66
        """
        val result = parse(receipt)
        assertEquals("STARBUCKS", result.merchant)
        assertEquals(11.66, result.amount)
        assertNotNull(result.date)
        assertEquals("cat_dining", result.suggestedCategoryId)
    }
}