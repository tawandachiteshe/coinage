package com.tawandachiteshe.coinage.feature.jars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.CategoryRepository
import com.tawandachiteshe.coinage.data.TransactionRepository
import com.tawandachiteshe.coinage.feature.home.Zoom
import com.tawandachiteshe.coinage.feature.home.dateRange
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class JarUi(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String,
    val budgetLimit: Double,
    val spent: Double,
    val isDefault: Boolean,
    val tilt: Float,
)

data class JarsState(
    val jars: List<JarUi> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface JarsAction {
    data class OnUpdateBudget(val jarId: String, val budget: Double) : JarsAction
    data class OnCreateJar(val name: String, val icon: String, val colorHex: String, val budget: Double) : JarsAction
    data class OnDeleteJar(val id: String) : JarsAction
}

sealed interface JarsEvent {
    data class ShowError(val msg: String) : JarsEvent
}

private val TILTS = listOf(-0.6f, 0.8f, -0.4f, 0.5f, -0.3f, 0.7f)

class JarsManagerViewModel(
    private val catRepo: CategoryRepository,
    private val txRepo: TransactionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(JarsState())
    val state: StateFlow<JarsState> = _state.asStateFlow()

    private val _events = Channel<JarsEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            catRepo.getExpenseJars().collect { categories ->
                val (start, end) = Zoom.Month.dateRange()
                val spending = try {
                    txRepo.getSpendingByCategory(start, end)
                } catch (_: Exception) {
                    emptyMap()
                }
                val jars = categories.mapIndexed { index, cat ->
                    JarUi(
                        id = cat.id,
                        name = cat.name,
                        icon = cat.icon,
                        colorHex = cat.color_hex,
                        budgetLimit = cat.budget_limit,
                        spent = spending[cat.id] ?: 0.0,
                        isDefault = cat.is_default == 1L,
                        tilt = TILTS[index % TILTS.size],
                    )
                }
                _state.update { it.copy(jars = jars, isLoading = false) }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAction(action: JarsAction) {
        when (action) {
            is JarsAction.OnUpdateBudget -> viewModelScope.launch {
                try {
                    catRepo.updateBudget(action.jarId, action.budget)
                } catch (e: Exception) {
                    _events.send(JarsEvent.ShowError("Failed to update budget: ${e.message}"))
                }
            }
            is JarsAction.OnCreateJar -> viewModelScope.launch {
                try {
                    catRepo.insert(
                        id = Uuid.random().toString(),
                        name = action.name,
                        icon = action.icon,
                        colorHex = action.colorHex,
                        type = "EXPENSE",
                        budgetLimit = action.budget,
                        isDefault = 0L,
                    )
                } catch (e: Exception) {
                    _events.send(JarsEvent.ShowError("Failed to create jar: ${e.message}"))
                }
            }
            is JarsAction.OnDeleteJar -> viewModelScope.launch {
                try {
                    catRepo.delete(action.id)
                } catch (e: Exception) {
                    _events.send(JarsEvent.ShowError("Failed to delete jar: ${e.message}"))
                }
            }
        }
    }
}