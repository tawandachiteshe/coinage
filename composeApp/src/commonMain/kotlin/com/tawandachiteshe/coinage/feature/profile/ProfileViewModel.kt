package com.tawandachiteshe.coinage.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.CategoryRepository
import com.tawandachiteshe.coinage.data.TransactionRepository
import com.tawandachiteshe.coinage.data.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val initial: String = "?",
    val totalTrackedLabel: String = "$0",
    val jarCount: Int = 0,
    val txCount: Long = 0L,
    val hasFirstSave: Boolean = false,
    val hasOnARoll: Boolean = false,
    val hasMountainMover: Boolean = false,
    val hasHalfFull: Boolean = false,
)

class ProfileViewModel(
    private val profileRepo: UserProfileRepository,
    private val txRepo: TransactionRepository,
    private val catRepo: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val name = profileRepo.getName()?.ifBlank { null } ?: "You"
            _state.update { it.copy(name = name, initial = name.first().uppercaseChar().toString()) }
        }
        viewModelScope.launch {
            txRepo.getAll()
                .combine(catRepo.getActiveExpenseJars()) { txs, jars ->
                    val total = txs.sumOf { it.amount }
                    val count = txs.size.toLong()
                    Triple(total, count, jars.size)
                }
                .collect { (total, count, jarCount) ->
                    _state.update { prev ->
                        prev.copy(
                            totalTrackedLabel = total.toTrackedLabel(),
                            jarCount = jarCount,
                            txCount = count,
                            hasFirstSave = count >= 1,
                            hasOnARoll = count >= 5,
                            hasMountainMover = total >= 1_000.0,
                            hasHalfFull = jarCount >= 3,
                        )
                    }
                }
        }
    }
}

private fun Double.toTrackedLabel(): String = when {
    this >= 1_000_000 -> "$${(this / 1_000_000).toInt()}M"
    this >= 1_000     -> "$${(this / 1_000).toInt()}k"
    else              -> "$${this.toInt()}"
}