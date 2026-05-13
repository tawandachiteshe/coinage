package com.tawandachiteshe.coinage.domain.models

import kotlinx.datetime.LocalDate

data class Expense(
    val id: String,
    val title: String,
    val amount: Double,
    val categoryId: String,
    val date: LocalDate,
    val note: String?,
)