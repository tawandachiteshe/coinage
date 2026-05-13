package com.tawandachiteshe.coinage.feature.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.DebtRepository
import com.tawandachiteshe.coinage.db.Debt
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

data class DebtUi(
    val id: String,
    val creditorName: String,
    val debtType: String,
    val principal: Double,
    val currentBalance: Double,
    val interestRate: Double,
    val minimumPayment: Double,
    val pctPaid: Float,
)

data class DebtState(
    val debts: List<DebtUi> = emptyList(),
    val totalOwed: Double = 0.0,
    val isSnowball: Boolean = true,
    val isLoading: Boolean = true,
)

sealed interface DebtAction {
    data class OnMakePayment(val debtId: String, val amount: Double) : DebtAction
    data class OnDeleteDebt(val id: String) : DebtAction
    data object OnToggleOrder : DebtAction
    data class OnCreateDebt(
        val creditor: String,
        val debtType: String,
        val principal: Double,
        val interestRate: Double,
        val minimumPayment: Double,
    ) : DebtAction
}

sealed interface DebtEvent {
    data class ShowError(val msg: String) : DebtEvent
}

class DebtViewModel(private val debtRepo: DebtRepository) : ViewModel() {

    private val _state = MutableStateFlow(DebtState())
    val state: StateFlow<DebtState> = _state.asStateFlow()

    private val _events = Channel<DebtEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            debtRepo.getSnowballOrder().collect { rows ->
                val total = rows.sumOf { it.current_balance }
                _state.update { s ->
                    s.copy(
                        debts = rows.map { it.toUi() },
                        totalOwed = total,
                        isLoading = false,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAction(action: DebtAction) {
        when (action) {
            is DebtAction.OnMakePayment -> viewModelScope.launch {
                val debt = _state.value.debts.firstOrNull { it.id == action.debtId } ?: return@launch
                val newBalance = (debt.currentBalance - action.amount).coerceAtLeast(0.0)
                debtRepo.updateBalance(action.debtId, newBalance)
            }

            is DebtAction.OnDeleteDebt ->
                viewModelScope.launch { debtRepo.delete(action.id) }

            is DebtAction.OnToggleOrder -> {
                _state.update { it.copy(isSnowball = !it.isSnowball) }
                viewModelScope.launch {
                    val rows = if (_state.value.isSnowball) debtRepo.getSnowballList()
                               else debtRepo.getAvalancheList()
                    _state.update { s -> s.copy(debts = rows.map { it.toUi() }) }
                }
            }

            is DebtAction.OnCreateDebt -> {
                if (action.creditor.isBlank()) {
                    viewModelScope.launch { _events.send(DebtEvent.ShowError("Creditor name can't be empty")) }
                    return
                }
                if (action.principal <= 0) {
                    viewModelScope.launch { _events.send(DebtEvent.ShowError("Balance must be > 0")) }
                    return
                }
                viewModelScope.launch {
                    val now = Clock.System.now().toEpochMilliseconds()
                    debtRepo.insert(
                        id = Uuid.random().toString(),
                        creditorName = action.creditor.trim(),
                        debtType = action.debtType,
                        principal = action.principal,
                        currentBalance = action.principal,
                        interestRate = action.interestRate,
                        minimumPayment = action.minimumPayment,
                        dueDate = null,
                        createdAt = now,
                    )
                }
            }
        }
    }

    private fun Debt.toUi() = DebtUi(
        id = id,
        creditorName = creditor_name,
        debtType = debt_type,
        principal = principal,
        currentBalance = current_balance,
        interestRate = interest_rate,
        minimumPayment = minimum_payment,
        pctPaid = if (principal > 0) (((principal - current_balance) / principal * 100.0).coerceIn(0.0, 100.0).toFloat()) else 0f,
    )
}