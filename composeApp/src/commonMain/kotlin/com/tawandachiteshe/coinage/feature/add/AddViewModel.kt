package com.tawandachiteshe.coinage.feature.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.CategoryRepository
import com.tawandachiteshe.coinage.feature.scan.ScannedReceipt
import com.tawandachiteshe.coinage.data.CurrencyRepository
import com.tawandachiteshe.coinage.data.DebtRepository
import com.tawandachiteshe.coinage.data.GoalRepository
import com.tawandachiteshe.coinage.data.TransactionRepository
import com.tawandachiteshe.coinage.db.Category
import com.tawandachiteshe.coinage.db.Currency
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class AddType { Transaction, Goal, Debt }
enum class TxType   { EXPENSE, INCOME }

data class CategoryUi(val id: String, val name: String, val icon: String, val colorHex: String, val type: String)
data class CurrencyUi(val code: String, val name: String, val symbol: String, val rateToUsd: Double, val isBase: Boolean)

data class AddState(
    val addType: AddType = AddType.Transaction,
    // --- transaction ---
    val merchant: String = "",
    val amount: String = "",
    val txType: TxType = TxType.EXPENSE,
    val selectedCategoryId: String? = null,   // expense category
    val selectedSourceId: String? = null,      // income source
    val notes: String = "",
    val scannedDateMs: Long? = null,     // null = use current time on save
    // --- goal ---
    val goalName: String = "",
    val goalTarget: String = "",
    val goalIcon: String = "target",
    // --- debt ---
    val debtCreditor: String = "",
    val debtBalance: String = "",
    val debtApr: String = "",
    val debtMinPayment: String = "",
    val debtType: String = "LOAN",
    // --- shared ---
    val expenseCategories: List<CategoryUi> = emptyList(),
    val incomeSources: List<CategoryUi> = emptyList(),
    val currencies: List<CurrencyUi> = emptyList(),
    val selectedCurrencyCode: String = "USD",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface AddAction {
    data class OnAddTypeChange(val type: AddType) : AddAction
    // transaction
    data class OnMerchantChange(val v: String) : AddAction
    data class OnAmountChange(val v: String) : AddAction
    data class OnTxTypeChange(val t: TxType) : AddAction
    data class OnCategorySelect(val id: String) : AddAction
    data class OnSourceSelect(val id: String) : AddAction
    data class OnCurrencyChange(val code: String) : AddAction
    data class OnNotesChange(val v: String) : AddAction
    // goal
    data class OnGoalNameChange(val v: String) : AddAction
    data class OnGoalTargetChange(val v: String) : AddAction
    data class OnGoalIconChange(val v: String) : AddAction
    // debt
    data class OnDebtCreditorChange(val v: String) : AddAction
    data class OnDebtBalanceChange(val v: String) : AddAction
    data class OnDebtAprChange(val v: String) : AddAction
    data class OnDebtMinPayChange(val v: String) : AddAction
    data class OnDebtTypeChange(val v: String) : AddAction
    data class OnScanResult(val receipt: ScannedReceipt) : AddAction
    // save
    data object OnSave : AddAction
    // lifecycle
    data class OnReset(val type: AddType) : AddAction
}

sealed interface AddEvent {
    data object Saved : AddEvent
    data class ShowError(val msg: String) : AddEvent
}

class AddViewModel(
    private val txRepo: TransactionRepository,
    private val goalRepo: GoalRepository,
    private val debtRepo: DebtRepository,
    private val catRepo: CategoryRepository,
    private val curRepo: CurrencyRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddState())
    val state: StateFlow<AddState> = _state.asStateFlow()

    private val _events = Channel<AddEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            catRepo.getAll().collect { cats ->
                val expenses = cats.filter { it.type == "EXPENSE" || it.type == "BOTH" }.map { it.toUi() }
                val sources  = cats.filter { it.type == "INCOME"  || it.type == "BOTH" }.map { it.toUi() }
                _state.update { s ->
                    s.copy(
                        expenseCategories = expenses,
                        incomeSources     = sources,
                        selectedCategoryId = s.selectedCategoryId ?: expenses.firstOrNull()?.id,
                        selectedSourceId   = s.selectedSourceId   ?: sources.firstOrNull()?.id,
                    )
                }
            }
        }
        viewModelScope.launch {
            curRepo.getAll().collect { currencies ->
                val base = currencies.firstOrNull { it.is_base == 1L }
                _state.update { s ->
                    s.copy(
                        currencies = currencies.map { it.toUi() },
                        selectedCurrencyCode = s.selectedCurrencyCode
                            .takeIf { c -> currencies.any { it.code == c } }
                            ?: base?.code ?: "USD",
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAction(action: AddAction) {
        when (action) {
            is AddAction.OnReset -> _state.update { cur ->
                AddState(
                    addType = action.type,
                    expenseCategories = cur.expenseCategories,
                    incomeSources = cur.incomeSources,
                    currencies = cur.currencies,
                    selectedCurrencyCode = cur.selectedCurrencyCode,
                    selectedCategoryId = cur.selectedCategoryId,
                    selectedSourceId = cur.selectedSourceId,
                    scannedDateMs = null,
                )
            }
            is AddAction.OnAddTypeChange  -> _state.update { it.copy(addType = action.type, error = null) }
            is AddAction.OnMerchantChange -> _state.update { it.copy(merchant = action.v, error = null) }
            is AddAction.OnAmountChange   -> _state.update { it.copy(amount = action.v, error = null) }
            is AddAction.OnTxTypeChange   -> _state.update { s ->
                s.copy(
                    txType = action.t,
                    selectedCategoryId = if (action.t == TxType.EXPENSE)
                        (s.selectedCategoryId ?: s.expenseCategories.firstOrNull()?.id)
                    else s.selectedCategoryId,
                    selectedSourceId = if (action.t == TxType.INCOME)
                        (s.selectedSourceId ?: s.incomeSources.firstOrNull()?.id)
                    else s.selectedSourceId,
                )
            }
            is AddAction.OnCategorySelect -> _state.update { it.copy(selectedCategoryId = action.id) }
            is AddAction.OnSourceSelect   -> _state.update { it.copy(selectedSourceId = action.id) }
            is AddAction.OnCurrencyChange -> _state.update { it.copy(selectedCurrencyCode = action.code) }
            is AddAction.OnNotesChange    -> _state.update { it.copy(notes = action.v) }
            is AddAction.OnGoalNameChange   -> _state.update { it.copy(goalName = action.v, error = null) }
            is AddAction.OnGoalTargetChange -> _state.update { it.copy(goalTarget = action.v, error = null) }
            is AddAction.OnGoalIconChange   -> _state.update { it.copy(goalIcon = action.v) }
            is AddAction.OnDebtCreditorChange -> _state.update { it.copy(debtCreditor = action.v, error = null) }
            is AddAction.OnDebtBalanceChange  -> _state.update { it.copy(debtBalance = action.v, error = null) }
            is AddAction.OnDebtAprChange      -> _state.update { it.copy(debtApr = action.v) }
            is AddAction.OnDebtMinPayChange   -> _state.update { it.copy(debtMinPayment = action.v) }
            is AddAction.OnDebtTypeChange     -> _state.update { it.copy(debtType = action.v) }
            is AddAction.OnScanResult -> {
                val r = action.receipt
                _state.update { s ->
                    s.copy(
                        merchant = r.merchant ?: s.merchant,
                        amount = r.amount?.let { "%.2f".format(it) } ?: s.amount,
                        txType = TxType.EXPENSE,
                        selectedCategoryId = r.suggestedCategoryId ?: s.selectedCategoryId,
                        scannedDateMs = r.date ?: s.scannedDateMs,
                    )
                }
            }
            is AddAction.OnSave -> save()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun save() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
            when (s.addType) {
                AddType.Transaction -> saveTransaction(s, now)
                AddType.Goal        -> saveGoal(s, now)
                AddType.Debt        -> saveDebt(s, now)
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun saveTransaction(s: AddState, now: Long) {
        val amount = s.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.update { it.copy(error = "Enter a valid amount") }
            return
        }
        val catId = if (s.txType == TxType.INCOME) s.selectedSourceId else s.selectedCategoryId
        if (catId == null) {
            _state.update { it.copy(error = if (s.txType == TxType.INCOME) "Select a source" else "Select a category") }
            return
        }
        txRepo.insert(
            id = Uuid.random().toString(),
            amount = amount,
            type = s.txType.name,
            categoryId = catId,
            merchant = s.merchant.trim().ifBlank { "Unknown" },
            notes = s.notes.trim().ifBlank { null },
            currencyCode = s.selectedCurrencyCode,
            date = s.scannedDateMs ?: now,
            createdAt = now,
        )
        _events.send(AddEvent.Saved)
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun saveGoal(s: AddState, now: Long) {
        if (s.goalName.isBlank()) {
            _state.update { it.copy(error = "Enter a goal name") }
            return
        }
        val target = s.goalTarget.toDoubleOrNull()
        if (target == null || target <= 0) {
            _state.update { it.copy(error = "Enter a valid target amount") }
            return
        }
        goalRepo.insert(
            id = Uuid.random().toString(),
            name = s.goalName.trim(),
            icon = s.goalIcon,
            targetAmount = target,
            savedAmount = 0.0,
            deadline = null,
            isCompleted = 0L,
            createdAt = now,
        )
        _events.send(AddEvent.Saved)
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun saveDebt(s: AddState, now: Long) {
        if (s.debtCreditor.isBlank()) {
            _state.update { it.copy(error = "Enter a creditor name") }
            return
        }
        val balance = s.debtBalance.toDoubleOrNull()
        if (balance == null || balance <= 0) {
            _state.update { it.copy(error = "Enter a valid balance") }
            return
        }
        debtRepo.insert(
            id = Uuid.random().toString(),
            creditorName = s.debtCreditor.trim(),
            debtType = s.debtType,
            principal = balance,
            currentBalance = balance,
            interestRate = s.debtApr.toDoubleOrNull() ?: 0.0,
            minimumPayment = s.debtMinPayment.toDoubleOrNull() ?: 0.0,
            dueDate = null,
            createdAt = now,
        )
        _events.send(AddEvent.Saved)
    }

    private fun Category.toUi() = CategoryUi(id = id, name = name, icon = icon, colorHex = color_hex, type = type)
    private fun Currency.toUi()  = CurrencyUi(code = code, name = name, symbol = symbol, rateToUsd = rate_to_usd, isBase = is_base == 1L)
}