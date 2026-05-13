package com.tawandachiteshe.coinage.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.CategoryRepository
import com.tawandachiteshe.coinage.data.TransactionRepository
import com.tawandachiteshe.coinage.feature.home.Zoom
import com.tawandachiteshe.coinage.feature.home.dateRange
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryTotalUi(
    val categoryId: String,
    val categoryName: String,
    val colorHex: String,
    val total: Double,
)

data class InsightsState(
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val categoryTotals: List<CategoryTotalUi> = emptyList(),
    val zoom: Zoom = Zoom.Month,
    val isLoading: Boolean = true,
    // Moments
    val biggestSplurgeMerchant: String? = null,
    val biggestSplurgeAmt: Double = 0.0,
    val mostVisitedMerchant: String? = null,
    val mostVisitedCount: Long = 0L,
    val mostVisitedTotal: Double = 0.0,
    val txCount: Long = 0L,
)

sealed interface InsightsAction {
    data class OnZoomChange(val zoom: Zoom) : InsightsAction
}

class InsightsViewModel(
    private val txRepo: TransactionRepository,
    private val catRepo: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(InsightsState())
    val state: StateFlow<InsightsState> = _state.asStateFlow()

    init {
        loadData(Zoom.Month)
    }

    fun onAction(action: InsightsAction) {
        when (action) {
            is InsightsAction.OnZoomChange -> {
                _state.update { it.copy(zoom = action.zoom, isLoading = true) }
                loadData(action.zoom)
            }
        }
    }

    private fun loadData(zoom: Zoom) {
        viewModelScope.launch {
            val (start, end) = zoom.dateRange()

            val categoriesDeferred = async { catRepo.getAll().first().associateBy { it.id } }
            val totalsDeferred     = async { txRepo.getCategoryTotals(start, end) }
            val incomeDeferred     = async { txRepo.getTotalByTypeAndDateRange("INCOME", start, end) }
            val biggestDeferred    = async { txRepo.getBiggestExpense(start, end) }
            val topMerchantDef     = async { txRepo.getTopMerchant(start, end) }
            val txCountDeferred    = async { txRepo.countInRange(start, end) }

            val categories  = categoriesDeferred.await()
            val totals      = totalsDeferred.await()
            val income      = incomeDeferred.await()
            val biggest     = biggestDeferred.await()
            val topMerchant = topMerchantDef.await()
            val txCount     = txCountDeferred.await()

            val totalSpent = totals.sumOf { it.total }
            val uiTotals = totals.mapNotNull { row ->
                val cat = categories[row.category_id] ?: return@mapNotNull null
                CategoryTotalUi(
                    categoryId = row.category_id,
                    categoryName = cat.name,
                    colorHex = cat.color_hex,
                    total = row.total,
                )
            }

            _state.update {
                it.copy(
                    totalSpent = totalSpent,
                    totalIncome = income,
                    categoryTotals = uiTotals,
                    isLoading = false,
                    biggestSplurgeMerchant = biggest?.merchant,
                    biggestSplurgeAmt = biggest?.amount ?: 0.0,
                    mostVisitedMerchant = topMerchant?.merchant,
                    mostVisitedCount = topMerchant?.visit_count ?: 0L,
                    mostVisitedTotal = topMerchant?.total_amount ?: 0.0,
                    txCount = txCount,
                )
            }
        }
    }
}