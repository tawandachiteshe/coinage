package com.tawandachiteshe.coinage.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long,
    val userProfile: BackupUserProfile? = null,
    val transactions: List<BackupTransaction> = emptyList(),
    val categories: List<BackupCategory> = emptyList(),
    val debts: List<BackupDebt> = emptyList(),
    val goals: List<BackupGoal> = emptyList(),
    val currencies: List<BackupCurrency> = emptyList(),
    val ious: List<BackupIou> = emptyList(),
)

@Serializable
data class BackupUserProfile(val name: String, val joinedAt: Long)

@Serializable
data class BackupTransaction(
    val id: String, val amount: Double, val type: String,
    val categoryId: String, val merchant: String, val notes: String?,
    val currencyCode: String, val date: Long, val createdAt: Long,
    val goalId: String? = null,
)

@Serializable
data class BackupCategory(
    val id: String, val name: String, val icon: String, val colorHex: String,
    val type: String, val budgetLimit: Double, val isDefault: Long, val isActive: Long,
)

@Serializable
data class BackupDebt(
    val id: String, val creditorName: String, val debtType: String,
    val principal: Double, val currentBalance: Double, val interestRate: Double,
    val minimumPayment: Double, val dueDate: Long?, val createdAt: Long,
)

@Serializable
data class BackupGoal(
    val id: String, val name: String, val icon: String,
    val targetAmount: Double, val savedAmount: Double,
    val deadline: Long?, val isCompleted: Long, val createdAt: Long,
)

@Serializable
data class BackupCurrency(
    val code: String, val name: String, val symbol: String,
    val rateToUsd: Double, val isBase: Long,
)

@Serializable
data class BackupIou(
    val id: String, val personName: String, val amount: Double,
    val paidAmount: Double, val notes: String?, val lentAt: Long,
    val dueDate: Long?, val createdAt: Long,
    val categoryId: String? = null,
)