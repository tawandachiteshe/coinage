package com.tawandachiteshe.coinage.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.CurrencyRepository
import com.tawandachiteshe.coinage.data.GoalRepository
import com.tawandachiteshe.coinage.data.TransactionRepository
import com.tawandachiteshe.coinage.db.Goal
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class GoalUi(
    val id: String,
    val name: String,
    val icon: String,
    val savedAmount: Double,
    val targetAmount: Double,
    val pct: Float,
    val due: String?,
    val isCompleted: Boolean,
)

data class GoalsState(
    val goals: List<GoalUi> = emptyList(),
    val totalSaved: Double = 0.0,
    val totalTarget: Double = 0.0,
    val availableBalance: Double = 0.0,
    val baseCurrencyCode: String = "USD",
    val isLoading: Boolean = true,
)

sealed interface GoalsAction {
    data class OnAddContribution(val goalId: String, val amount: Double) : GoalsAction
    data class OnDeleteGoal(val id: String) : GoalsAction
    data class OnArchiveGoal(val id: String) : GoalsAction
    data class OnCreateGoal(
        val name: String,
        val icon: String,
        val targetAmount: Double,
        val deadlineMs: Long?,
    ) : GoalsAction
}

sealed interface GoalsEvent {
    data class ShowError(val msg: String) : GoalsEvent
}

class GoalsViewModel(
    private val goalRepo: GoalRepository,
    private val txRepo: TransactionRepository,
    private val currencyRepo: CurrencyRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(GoalsState())
    val state: StateFlow<GoalsState> = _state.asStateFlow()

    private val _events = Channel<GoalsEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            currencyRepo.getBase().collect { base ->
                if (base != null) _state.update { it.copy(baseCurrencyCode = base.code) }
            }
        }
        // Combine goal rows, transaction-derived savings, and live balance so the UI
        // always reflects what the user can actually afford to contribute.
        viewModelScope.launch {
            combine(
                goalRepo.getAll(),
                txRepo.getSavingsPerGoalFlow(),
                txRepo.getBalanceFlow(),
            ) { rows, savingsMap, balance -> Triple(rows, savingsMap, balance) }
                .collect { (rows, savingsMap, balance) ->
                    val goals = rows.map { it.toUi(savingsMap) }
                    _state.update { s ->
                        s.copy(
                            goals = goals,
                            totalSaved = goals.sumOf { it.savedAmount },
                            totalTarget = goals.sumOf { it.targetAmount },
                            availableBalance = balance.coerceAtLeast(0.0),
                            isLoading = false,
                        )
                    }
                }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAction(action: GoalsAction) {
        when (action) {
            is GoalsAction.OnAddContribution -> {
                val goal = _state.value.goals.find { it.id == action.goalId } ?: return
                val remaining = goal.targetAmount - goal.savedAmount
                if (remaining <= 0) return
                val available = _state.value.availableBalance
                if (available <= 0) {
                    viewModelScope.launch { _events.send(GoalsEvent.ShowError("No funds available to contribute")) }
                    return
                }
                val amount = action.amount.coerceAtMost(remaining).coerceAtMost(available)
                viewModelScope.launch {
                    val now = Clock.System.now().toEpochMilliseconds()
                    txRepo.insert(
                        id = Uuid.random().toString(),
                        amount = amount,
                        type = "EXPENSE",
                        categoryId = "cat_savings",
                        merchant = goal.name,
                        notes = null,
                        currencyCode = _state.value.baseCurrencyCode,
                        date = now,
                        createdAt = now,
                        goalId = action.goalId,
                    )
                }
            }

            is GoalsAction.OnDeleteGoal ->
                viewModelScope.launch { goalRepo.delete(action.id) }

            is GoalsAction.OnArchiveGoal ->
                viewModelScope.launch { goalRepo.archive(action.id) }

            is GoalsAction.OnCreateGoal -> {
                if (action.name.isBlank()) {
                    viewModelScope.launch { _events.send(GoalsEvent.ShowError("Name can't be empty")) }
                    return
                }
                if (action.targetAmount <= 0) {
                    viewModelScope.launch { _events.send(GoalsEvent.ShowError("Target must be > 0")) }
                    return
                }
                viewModelScope.launch {
                    val now = Clock.System.now().toEpochMilliseconds()
                    goalRepo.insert(
                        id = Uuid.random().toString(),
                        name = action.name.trim(),
                        icon = action.icon,
                        targetAmount = action.targetAmount,
                        savedAmount = 0.0,
                        deadline = action.deadlineMs,
                        isCompleted = 0L,
                        createdAt = now,
                    )
                }
            }
        }
    }

    private fun Goal.toUi(savingsMap: Map<String, Double>): GoalUi {
        val saved = savingsMap[id] ?: 0.0
        val pct = if (target_amount > 0) (saved / target_amount * 100.0).coerceIn(0.0, 100.0).toFloat() else 0f
        return GoalUi(
            id = id,
            name = name,
            icon = icon,
            savedAmount = saved,
            targetAmount = target_amount,
            pct = pct,
            due = deadline?.toString(),
            isCompleted = saved >= target_amount,
        )
    }
}