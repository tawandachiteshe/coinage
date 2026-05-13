package com.tawandachiteshe.coinage.feature.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.CategoryRepository
import com.tawandachiteshe.coinage.data.DebtRepository
import com.tawandachiteshe.coinage.data.GoalRepository
import com.tawandachiteshe.coinage.data.TransactionRepository
import com.tawandachiteshe.coinage.db.Category
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class AddType { Transaction, Goal, Debt }
enum class TxType   { EXPENSE, INCOME }

data class CategoryUi(val id: String, val name: String, val icon: String, val colorHex: String, val type: String)

data class AddState(
    val addType: AddType = AddType.Transaction,
    // --- transaction ---
    val merchant: String = "",
    val amount: String = "",
    val txType: TxType = TxType.EXPENSE,
    val selectedCategoryId: String? = null,
    val notes: String = "",
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
    val categories: List<CategoryUi> = emptyList(),
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
    // save
    data object OnSave : AddAction
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
) : ViewModel() {

    private val _state = MutableStateFlow(AddState())
    val state: StateFlow<AddState> = _state.asStateFlow()

    private val _events = Channel<AddEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            catRepo.getAll().collect { cats ->
                _state.update { s ->
                    s.copy(
                        categories = cats.map { it.toUi() },
                        selectedCategoryId = s.selectedCategoryId
                            ?: cats.firstOrNull { it.type == "EXPENSE" || it.type == "BOTH" }?.id,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAction(action: AddAction) {
        when (action) {
            is AddAction.OnAddTypeChange  -> _state.update { it.copy(addType = action.type, error = null) }
            is AddAction.OnMerchantChange -> _state.update { it.copy(merchant = action.v, error = null) }
            is AddAction.OnAmountChange   -> _state.update { it.copy(amount = action.v, error = null) }
            is AddAction.OnTxTypeChange   -> _state.update { it.copy(txType = action.t) }
            is AddAction.OnCategorySelect -> _state.update { it.copy(selectedCategoryId = action.id) }
            is AddAction.OnNotesChange    -> _state.update { it.copy(notes = action.v) }
            is AddAction.OnGoalNameChange -> _state.update { it.copy(goalName = action.v, error = null) }
            is AddAction.OnGoalTargetChange -> _state.update { it.copy(goalTarget = action.v, error = null) }
            is AddAction.OnGoalIconChange -> _state.update { it.copy(goalIcon = action.v) }
            is AddAction.OnDebtCreditorChange -> _state.update { it.copy(debtCreditor = action.v, error = null) }
            is AddAction.OnDebtBalanceChange  -> _state.update { it.copy(debtBalance = action.v, error = null) }
            is AddAction.OnDebtAprChange      -> _state.update { it.copy(debtApr = action.v) }
            is AddAction.OnDebtMinPayChange   -> _state.update { it.copy(debtMinPayment = action.v) }
            is AddAction.OnDebtTypeChange     -> _state.update { it.copy(debtType = action.v) }
            is AddAction.OnSave -> save()
        }
    }

    private fun save() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val now = Clock.System.now().toEpochMilliseconds()
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
        val catId = s.selectedCategoryId
        if (catId == null) {
            _state.update { it.copy(error = "Select a category") }
            return
        }
        txRepo.insert(
            id = Uuid.random().toString(),
            amount = amount,
            type = s.txType.name,
            categoryId = catId,
            merchant = s.merchant.trim().ifBlank { "Unknown" },
            notes = s.notes.trim().ifBlank { null },
            date = now,
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
}