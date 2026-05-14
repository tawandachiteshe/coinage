package com.tawandachiteshe.coinage.feature.scan

data class ScannedReceipt(
    val merchant: String?,
    val amount: Double?,
    val date: Long?,
    val suggestedCategoryId: String?,
    val rawText: String,
)