package com.tawandachiteshe.expensify.domain.models

data class Category(
    val id: String,
    val name: String,
    val iconName: String,
    val colorHex: String,
)

val defaultCategories = listOf(
    Category("food", "Food & Dining", "restaurant", "0xFFE57373"),
    Category("transport", "Transport", "directions_car", "0xFF64B5F6"),
    Category("shopping", "Shopping", "shopping_bag", "0xFFBA68C8"),
    Category("health", "Health", "favorite", "0xFF81C784"),
    Category("entertainment", "Entertainment", "movie", "0xFFFFB74D"),
    Category("utilities", "Utilities", "bolt", "0xFF4DB6AC"),
    Category("other", "Other", "category", "0xFF90A4AE"),
)