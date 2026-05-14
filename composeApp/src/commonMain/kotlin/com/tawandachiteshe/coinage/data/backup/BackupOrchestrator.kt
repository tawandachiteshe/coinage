package com.tawandachiteshe.coinage.data.backup

import com.tawandachiteshe.coinage.data.CategoryRepository
import com.tawandachiteshe.coinage.data.CurrencyRepository
import com.tawandachiteshe.coinage.data.DebtRepository
import com.tawandachiteshe.coinage.data.GoalRepository
import com.tawandachiteshe.coinage.data.TransactionRepository
import com.tawandachiteshe.coinage.data.UserProfileRepository
import com.tawandachiteshe.coinage.domain.DataError
import com.tawandachiteshe.coinage.domain.Result
import com.tawandachiteshe.coinage.domain.repository.DriveRepository
import com.tawandachiteshe.coinage.domain.repository.SheetsRepository
import kotlinx.datetime.Clock

class BackupOrchestrator(
    private val txRepo: TransactionRepository,
    private val catRepo: CategoryRepository,
    private val debtRepo: DebtRepository,
    private val goalRepo: GoalRepository,
    private val currencyRepo: CurrencyRepository,
    private val profileRepo: UserProfileRepository,
    private val driveRepo: DriveRepository,
    private val sheetsRepo: SheetsRepository,
) {
    suspend fun backup(): Result<Unit, DataError.Network> {
        val name = profileRepo.getName()?.ifBlank { null }
        val joinedAt = profileRepo.getJoinedAt() ?: 0L
        val data = BackupData(
            exportedAt   = Clock.System.now().toEpochMilliseconds(),
            userProfile  = name?.let { BackupUserProfile(it, joinedAt) },
            transactions = txRepo.getAllOnce().map {
                BackupTransaction(it.id, it.amount, it.type, it.category_id,
                    it.merchant, it.notes, it.currency_code, it.date, it.created_at)
            },
            categories = catRepo.getAllOnce().map {
                BackupCategory(it.id, it.name, it.icon, it.color_hex,
                    it.type, it.budget_limit, it.is_default, it.is_active)
            },
            debts = debtRepo.getAllOnce().map {
                BackupDebt(it.id, it.creditor_name, it.debt_type, it.principal,
                    it.current_balance, it.interest_rate, it.minimum_payment, it.due_date, it.created_at)
            },
            goals = goalRepo.getAllOnce().map {
                BackupGoal(it.id, it.name, it.icon, it.target_amount,
                    it.saved_amount, it.deadline, it.is_completed, it.created_at)
            },
            currencies = currencyRepo.getAllOnce().map {
                BackupCurrency(it.code, it.name, it.symbol, it.rate_to_usd, it.is_base)
            },
        )
        return driveRepo.backup(data)
    }

    suspend fun restore(): Result<Unit, DataError.Network> {
        val result = driveRepo.restore()
        if (result is Result.Error) return result
        val data = (result as Result.Success).data

        // Restore order: categories first (transactions reference them via FK)
        catRepo.deleteAll()
        data.categories.forEach { c ->
            catRepo.insert(c.id, c.name, c.icon, c.colorHex, c.type, c.budgetLimit, c.isDefault, c.isActive)
        }

        txRepo.deleteAll()
        data.transactions.forEach { t ->
            txRepo.insert(t.id, t.amount, t.type, t.categoryId,
                t.merchant, t.notes, t.currencyCode, t.date, t.createdAt)
        }

        debtRepo.deleteAll()
        data.debts.forEach { d ->
            debtRepo.insert(d.id, d.creditorName, d.debtType, d.principal,
                d.currentBalance, d.interestRate, d.minimumPayment, d.dueDate, d.createdAt)
        }

        goalRepo.deleteAll()
        data.goals.forEach { g ->
            goalRepo.insert(g.id, g.name, g.icon, g.targetAmount,
                g.savedAmount, g.deadline, g.isCompleted, g.createdAt)
        }

        currencyRepo.deleteAll()
        data.currencies.forEach { c ->
            currencyRepo.insert(c.code, c.name, c.symbol, c.rateToUsd, c.isBase)
        }

        data.userProfile?.let { profileRepo.restoreProfile(it.name, it.joinedAt) }
        return Result.Success(Unit)
    }

    suspend fun syncToSheets(): Result<String, DataError.Network> {
        val name = profileRepo.getName()?.ifBlank { null }
        val joinedAt = profileRepo.getJoinedAt() ?: 0L
        val data = BackupData(
            exportedAt   = Clock.System.now().toEpochMilliseconds(),
            userProfile  = name?.let { BackupUserProfile(it, joinedAt) },
            transactions = txRepo.getAllOnce().map {
                BackupTransaction(it.id, it.amount, it.type, it.category_id,
                    it.merchant, it.notes, it.currency_code, it.date, it.created_at)
            },
            categories = catRepo.getAllOnce().map {
                BackupCategory(it.id, it.name, it.icon, it.color_hex,
                    it.type, it.budget_limit, it.is_default, it.is_active)
            },
            debts = debtRepo.getAllOnce().map {
                BackupDebt(it.id, it.creditor_name, it.debt_type, it.principal,
                    it.current_balance, it.interest_rate, it.minimum_payment, it.due_date, it.created_at)
            },
            goals = goalRepo.getAllOnce().map {
                BackupGoal(it.id, it.name, it.icon, it.target_amount,
                    it.saved_amount, it.deadline, it.is_completed, it.created_at)
            },
            currencies = emptyList(),
        )
        return sheetsRepo.sync(data)
    }

    suspend fun lastBackupInfo() = driveRepo.lastBackupInfo()
}