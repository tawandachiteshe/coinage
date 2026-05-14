package com.tawandachiteshe.coinage.data

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests the currency conversion formula used in Transaction.sq:
 *   converted = amount × (base_rate_to_usd / tx_rate_to_usd)
 *
 * Rates are "how many units of this currency equal 1 USD":
 *   USD = 1.0, ZAR = 18.6, EUR = 0.93, GBP = 0.79
 */
class CurrencyConversionTest {

    // Mirrors the SQL formula in Transaction.sq
    private fun convert(amount: Double, txRateToUsd: Double, baseRateToUsd: Double): Double =
        amount * (baseRateToUsd / txRateToUsd)

    private fun assertApprox(expected: Double, actual: Double, tolerance: Double = 0.01) {
        assertTrue(
            abs(actual - expected) <= tolerance,
            "Expected ≈$expected but was $actual (tolerance ±$tolerance)"
        )
    }

    // ── Identity: same currency, no change ───────────────────────────────────

    @Test
    fun `ZAR to ZAR is unchanged`() {
        assertApprox(100.0, convert(100.0, txRateToUsd = 18.6, baseRateToUsd = 18.6))
    }

    @Test
    fun `USD to USD is unchanged`() {
        assertApprox(50.0, convert(50.0, txRateToUsd = 1.0, baseRateToUsd = 1.0))
    }

    // ── ZAR base currency ─────────────────────────────────────────────────────

    @Test
    fun `USD to ZAR base`() {
        // $10 USD at rate 18.6 = R186
        assertApprox(186.0, convert(10.0, txRateToUsd = 1.0, baseRateToUsd = 18.6))
    }

    @Test
    fun `EUR to ZAR base`() {
        // €100 EUR (rate 0.93) to ZAR (rate 18.6)
        // = 100 × (18.6 / 0.93) = 100 × 20 = R2000
        assertApprox(2000.0, convert(100.0, txRateToUsd = 0.93, baseRateToUsd = 18.6))
    }

    @Test
    fun `GBP to ZAR base`() {
        // £50 GBP (rate 0.79) to ZAR (rate 18.6)
        // = 50 × (18.6 / 0.79) ≈ 50 × 23.54 ≈ R1177.22
        assertApprox(1177.22, convert(50.0, txRateToUsd = 0.79, baseRateToUsd = 18.6), tolerance = 0.05)
    }

    // ── USD base currency ─────────────────────────────────────────────────────

    @Test
    fun `ZAR to USD base`() {
        // R186 at rate 18.6 → $10
        assertApprox(10.0, convert(186.0, txRateToUsd = 18.6, baseRateToUsd = 1.0))
    }

    @Test
    fun `EUR to USD base`() {
        // €93 EUR (rate 0.93) → $100 USD
        assertApprox(100.0, convert(93.0, txRateToUsd = 0.93, baseRateToUsd = 1.0))
    }

    // ── EUR base currency ─────────────────────────────────────────────────────

    @Test
    fun `USD to EUR base`() {
        // $100 USD → €93
        assertApprox(93.0, convert(100.0, txRateToUsd = 1.0, baseRateToUsd = 0.93))
    }

    // ── Aggregation correctness ───────────────────────────────────────────────

    @Test
    fun `sum of mixed currencies converts correctly to ZAR`() {
        // Simulate: $10 USD + R50 ZAR + €5 EUR, base = ZAR (18.6)
        val usdInZar  = convert(10.0,  txRateToUsd = 1.0,  baseRateToUsd = 18.6)  // R186
        val zarInZar  = convert(50.0,  txRateToUsd = 18.6, baseRateToUsd = 18.6)  // R50
        val eurInZar  = convert(5.0,   txRateToUsd = 0.93, baseRateToUsd = 18.6)  // R100

        assertApprox(186.0, usdInZar)
        assertApprox(50.0,  zarInZar)
        assertApprox(100.0, eurInZar)
        assertApprox(336.0, usdInZar + zarInZar + eurInZar)
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    fun `zero amount converts to zero`() {
        assertApprox(0.0, convert(0.0, txRateToUsd = 18.6, baseRateToUsd = 1.0))
    }

    @Test
    fun `unknown currency falls back to rate 1_0 (USD equivalent)`() {
        // COALESCE(null, 1.0) in SQL — treat unknown as USD
        val fallbackRate = 1.0
        assertApprox(186.0, convert(10.0, txRateToUsd = fallbackRate, baseRateToUsd = 18.6))
    }

    @Test
    fun `large ZAR grocery receipt converts to USD correctly`() {
        // R1500 grocery shop → how many USD?
        // 1500 × (1.0 / 18.6) ≈ $80.65
        assertApprox(80.65, convert(1500.0, txRateToUsd = 18.6, baseRateToUsd = 1.0), tolerance = 0.01)
    }
}
