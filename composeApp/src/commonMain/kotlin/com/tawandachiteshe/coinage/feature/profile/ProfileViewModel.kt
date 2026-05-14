package com.tawandachiteshe.coinage.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.CategoryRepository
import com.tawandachiteshe.coinage.data.TransactionRepository
import com.tawandachiteshe.coinage.data.UserProfileRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

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
    val hasStreakKeeper: Boolean = false,
    val hasBigSpender: Boolean = false,
    val hasLongHauler: Boolean = false,
    val hasJarMaster: Boolean = false,
)

sealed interface ProfileEvent {
    data class BadgeEarned(val label: String) : ProfileEvent
}

class ProfileViewModel(
    private val profileRepo: UserProfileRepository,
    private val txRepo: TransactionRepository,
    private val catRepo: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _events = Channel<ProfileEvent>(BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val name = profileRepo.getName()?.ifBlank { null } ?: "You"
            val joinedAt = profileRepo.getJoinedAt() ?: 0L
            _state.update { it.copy(name = name, initial = name.first().uppercaseChar().toString()) }

            var isFirstEmission = true

            txRepo.getAll()
                .combine(catRepo.getActiveExpenseJars()) { txs, jars ->
                    val total = txs.sumOf { it.amount }
                    val count = txs.size.toLong()
                    val epochDays = txs.map { it.date / 86_400_000L }.toSet()
                    val maxSingle = txs.filter { it.type == "EXPENSE" }.maxOfOrNull { it.amount } ?: 0.0
                    val jarsWithTx = txs.map { it.category_id }.toSet()
                    val activeJarIds = jars.map { it.id }.toSet()
                    val daysUsed = if (joinedAt > 0L)
                        (Clock.System.now().toEpochMilliseconds() - joinedAt) / 86_400_000L
                        else 0L
                    Tuple(total, count, jars.size, epochDays, maxSingle, jarsWithTx, activeJarIds, daysUsed)
                }
                .collect { (total, count, jarCount, epochDays, maxSingle, jarsWithTx, activeJarIds, daysUsed) ->
                    val prev = _state.value
                    val next = prev.copy(
                        totalTrackedLabel = total.toTrackedLabel(),
                        jarCount = jarCount,
                        txCount = count,
                        hasFirstSave = count >= 1,
                        hasOnARoll = count >= 5,
                        hasMountainMover = total >= 1_000.0,
                        hasHalfFull = jarCount >= 3,
                        hasStreakKeeper = hasConsecutiveDayStreak(epochDays, 7),
                        hasBigSpender = maxSingle > 500.0,
                        hasLongHauler = daysUsed >= 30,
                        hasJarMaster = activeJarIds.isNotEmpty() && jarsWithTx.containsAll(activeJarIds),
                    )
                    _state.value = next

                    if (isFirstEmission) {
                        isFirstEmission = false
                    } else {
                        BADGE_CHECKS.forEach { (getPrev, getNext, label) ->
                            if (!getPrev(prev) && getNext(next)) {
                                _events.trySend(ProfileEvent.BadgeEarned(label))
                            }
                        }
                    }
                }
        }
    }
}

private val BADGE_CHECKS: List<Triple<(ProfileUiState) -> Boolean, (ProfileUiState) -> Boolean, String>> = listOf(
    Triple({ it.hasFirstSave },     { it.hasFirstSave },     "first save"),
    Triple({ it.hasOnARoll },       { it.hasOnARoll },       "on a roll"),
    Triple({ it.hasMountainMover }, { it.hasMountainMover }, "mountain mover"),
    Triple({ it.hasHalfFull },      { it.hasHalfFull },      "half full"),
    Triple({ it.hasStreakKeeper },   { it.hasStreakKeeper },  "streak keeper"),
    Triple({ it.hasBigSpender },    { it.hasBigSpender },    "big spender"),
    Triple({ it.hasLongHauler },    { it.hasLongHauler },    "long hauler"),
    Triple({ it.hasJarMaster },     { it.hasJarMaster },     "jar master"),
)

private data class Tuple(
    val total: Double,
    val count: Long,
    val jarCount: Int,
    val epochDays: Set<Long>,
    val maxSingle: Double,
    val jarsWithTx: Set<String>,
    val activeJarIds: Set<String>,
    val daysUsed: Long,
)

internal fun hasConsecutiveDayStreak(epochDays: Set<Long>, n: Int): Boolean {
    if (epochDays.size < n) return false
    val sorted = epochDays.sorted()
    var streak = 1
    if (streak >= n) return true  // handles n == 1
    for (i in 1 until sorted.size) {
        streak = if (sorted[i] == sorted[i - 1] + 1) streak + 1 else 1
        if (streak >= n) return true
    }
    return false
}

private fun Double.toTrackedLabel(): String = when {
    this >= 1_000_000 -> "$${(this / 1_000_000).toInt()}M"
    this >= 1_000     -> "$${(this / 1_000).toInt()}k"
    else              -> "$${this.toInt()}"
}