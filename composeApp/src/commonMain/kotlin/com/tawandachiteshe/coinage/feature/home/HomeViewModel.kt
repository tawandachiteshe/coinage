package com.tawandachiteshe.coinage.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.CategoryRepository
import com.tawandachiteshe.coinage.data.CurrencyRepository
import com.tawandachiteshe.coinage.data.TransactionRepository
import com.tawandachiteshe.coinage.db.SelectAll
import com.tawandachiteshe.coinage.feature.jars.JarUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

data class TransactionUi(
    val id: String,
    val merchant: String,
    val amount: Double,
    val currencyCode: String,
    val type: String,
    val categoryName: String,
    val categoryIcon: String,
    val date: Long,
)

enum class Zoom { Year, Month, Week }

fun Zoom.dateRange(): Pair<Long, Long> {
    val now = Clock.System.now()
    val tz = TimeZone.currentSystemDefault()
    val local = now.toLocalDateTime(tz)
    val startInstant = when (this) {
        Zoom.Week -> {
            val daysBack = local.dayOfWeek.ordinal // 0=Monday … 6=Sunday
            val weekStartInstant = now.minus(daysBack.days)
            val d = weekStartInstant.toLocalDateTime(tz)
            LocalDateTime(d.year, d.monthNumber, d.dayOfMonth, 0, 0, 0).toInstant(tz)
        }
        Zoom.Month -> LocalDateTime(local.year, local.monthNumber, 1, 0, 0, 0).toInstant(tz)
        Zoom.Year  -> LocalDateTime(local.year, 1, 1, 0, 0, 0).toInstant(tz)
    }
    return startInstant.toEpochMilliseconds() to now.toEpochMilliseconds()
}

private val JAR_TILTS = listOf(-1.5f, 1.0f, -0.8f, 1.5f, -1.0f, 0.8f)

data class HomeState(
    val balance: Double = 0.0,
    val income: Double = 0.0,
    val expenses: Double = 0.0,
    val zoom: Zoom = Zoom.Month,
    val transactions: List<TransactionUi> = emptyList(),
    val jars: List<JarUi> = emptyList(),
    val baseCurrencySymbol: String = "$",
    val baseCurrencyCode: String = "USD",
    val isLoading: Boolean = true,
)

sealed interface HomeAction {
    data class OnZoomChange(val zoom: Zoom) : HomeAction
    data class OnDeleteTransaction(val id: String) : HomeAction
}

class HomeViewModel(
    private val txRepo: TransactionRepository,
    private val catRepo: CategoryRepository,
    private val curRepo: CurrencyRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            curRepo.getBase().collect { base ->
                if (base != null) {
                    _state.update { it.copy(baseCurrencySymbol = base.symbol, baseCurrencyCode = base.code) }
                }
            }
        }
        viewModelScope.launch {
            txRepo.getAll().collect { rows ->
                _state.update { s -> s.copy(transactions = rows.map { it.toUi() }, isLoading = false) }
            }
        }
        viewModelScope.launch {
            _state.map { it.zoom }
                .distinctUntilChanged()
                .collectLatest { zoom ->
                    val (start, end) = zoom.dateRange()
                    val income   = txRepo.getTotalByTypeAndDateRange("INCOME",  start, end)
                    val expenses = txRepo.getTotalByTypeAndDateRange("EXPENSE", start, end)
                    _state.update { it.copy(income = income, expenses = expenses, balance = income - expenses) }
                }
        }
        viewModelScope.launch {
            catRepo.getActiveExpenseJars().collect { categories ->
                val zoom = _state.value.zoom
                val (start, end) = zoom.dateRange()
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
                        isActive = true,
                        tilt = JAR_TILTS[index % JAR_TILTS.size],
                    )
                }
                _state.update { it.copy(jars = jars) }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.OnZoomChange ->
                _state.update { it.copy(zoom = action.zoom) }
            is HomeAction.OnDeleteTransaction ->
                viewModelScope.launch { txRepo.delete(action.id) }
        }
    }

    private fun SelectAll.toUi() = TransactionUi(
        id = id,
        merchant = merchant,
        amount = amount,
        currencyCode = currency_code,
        type = type,
        categoryName = category_name,
        categoryIcon = category_icon,
        date = date,
    )
}