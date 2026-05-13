package com.tawandachiteshe.coinage.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.GoalRepository
import com.tawandachiteshe.coinage.db.Goal
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
    val isLoading: Boolean = true,
)

sealed interface GoalsAction {
    data class OnAddContribution(val goalId: String, val amount: Double) : GoalsAction
    data class OnDeleteGoal(val id: String) : GoalsAction
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

class GoalsViewModel(private val goalRepo: GoalRepository) : ViewModel() {

    private val _state = MutableStateFlow(GoalsState())
    val state: StateFlow<GoalsState> = _state.asStateFlow()

    private val _events = Channel<GoalsEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            goalRepo.getAll().collect { rows ->
                _state.update { s ->
                    s.copy(
                        goals = rows.map { it.toUi() },
                        totalSaved = rows.sumOf { it.saved_amount },
                        totalTarget = rows.sumOf { it.target_amount },
                        isLoading = false,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAction(action: GoalsAction) {
        when (action) {
            is GoalsAction.OnAddContribution ->
                viewModelScope.launch {
                    goalRepo.addToSaved(action.goalId, action.amount)
                }

            is GoalsAction.OnDeleteGoal ->
                viewModelScope.launch { goalRepo.delete(action.id) }

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

    private fun Goal.toUi() = GoalUi(
        id = id,
        name = name,
        icon = icon,
        savedAmount = saved_amount,
        targetAmount = target_amount,
        pct = if (target_amount > 0) ((saved_amount / target_amount * 100.0).coerceIn(0.0, 100.0).toFloat()) else 0f,
        due = deadline?.toString(),
        isCompleted = is_completed == 1L,
    )
}