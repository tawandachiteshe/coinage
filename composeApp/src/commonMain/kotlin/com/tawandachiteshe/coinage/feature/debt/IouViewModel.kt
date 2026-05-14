package com.tawandachiteshe.coinage.feature.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.CategoryRepository
import com.tawandachiteshe.coinage.data.CurrencyRepository
import com.tawandachiteshe.coinage.data.IouRepository
import com.tawandachiteshe.coinage.data.TransactionRepository
import com.tawandachiteshe.coinage.db.Iou
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class IouCategoryOption(val id: String, val name: String)

data class IouUi(
    val id: String,
    val personName: String,
    val amount: Double,
    val paidAmount: Double,
    val outstanding: Double,
    val pctRepaid: Float,
    val isSettled: Boolean,
    val notes: String?,
    val categoryId: String?,
    val categoryName: String?,
    val lentAt: Long,
)

data class IouState(
    val ious: List<IouUi> = emptyList(),
    val totalOutstanding: Double = 0.0,
    val incomeCats: List<IouCategoryOption> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface IouAction {
    data class OnRecordPayment(val id: String, val amount: Double) : IouAction
    data class OnDeleteIou(val id: String) : IouAction
    data class OnCreateIou(
        val personName: String,
        val amount: Double,
        val notes: String?,
        val categoryId: String?,
    ) : IouAction
}

class IouViewModel(
    private val iouRepo: IouRepository,
    private val txRepo: TransactionRepository,
    private val currencyRepo: CurrencyRepository,
    private val catRepo: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(IouState())
    val state: StateFlow<IouState> = _state.asStateFlow()

    private var baseCurrencyCode = "USD"

    init {
        viewModelScope.launch {
            currencyRepo.getBase().collect { base ->
                if (base != null) baseCurrencyCode = base.code
            }
        }
        viewModelScope.launch {
            catRepo.getByType("INCOME").collect { cats ->
                _state.update { it.copy(incomeCats = cats.map { c -> IouCategoryOption(c.id, c.name) }) }
            }
        }
        viewModelScope.launch {
            combine(
                iouRepo.getAll(),
                iouRepo.getTotalOutstandingFlow(),
            ) { rows, total -> rows to total }
                .collect { (rows, total) ->
                    val catMap = _state.value.incomeCats.associateBy { it.id }
                    _state.update {
                        it.copy(
                            ious = rows.map { r -> r.toUi(catMap) },
                            totalOutstanding = total,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAction(action: IouAction) {
        when (action) {
            is IouAction.OnRecordPayment -> viewModelScope.launch {
                val iou = _state.value.ious.firstOrNull { it.id == action.id }
                val capped = action.amount.coerceAtMost(iou?.outstanding ?: action.amount)
                iouRepo.recordPayment(action.id, capped)
                val now = Clock.System.now().toEpochMilliseconds()
                txRepo.insert(
                    id = Uuid.random().toString(),
                    amount = capped,
                    type = "INCOME",
                    categoryId = iou?.categoryId ?: "cat_other",
                    merchant = iou?.personName ?: "IOU",
                    notes = "IOU repayment received",
                    currencyCode = baseCurrencyCode,
                    date = now,
                    createdAt = now,
                    goalId = null,
                )
            }

            is IouAction.OnDeleteIou -> viewModelScope.launch {
                iouRepo.delete(action.id)
            }

            is IouAction.OnCreateIou -> {
                if (action.personName.isBlank() || action.amount <= 0) return
                viewModelScope.launch {
                    val now = Clock.System.now().toEpochMilliseconds()
                    iouRepo.insert(
                        id = Uuid.random().toString(),
                        personName = action.personName.trim(),
                        amount = action.amount,
                        notes = action.notes?.trim()?.ifBlank { null },
                        categoryId = action.categoryId,
                        lentAt = now,
                        dueDate = null,
                        createdAt = now,
                    )
                }
            }
        }
    }

    private fun Iou.toUi(catMap: Map<String, IouCategoryOption>): IouUi {
        val outstanding = (amount - paid_amount).coerceAtLeast(0.0)
        val pct = if (amount > 0) ((paid_amount / amount) * 100.0).coerceIn(0.0, 100.0).toFloat() else 0f
        return IouUi(
            id = id,
            personName = person_name,
            amount = amount,
            paidAmount = paid_amount,
            outstanding = outstanding,
            pctRepaid = pct,
            isSettled = outstanding == 0.0,
            notes = notes,
            categoryId = category_id,
            categoryName = category_id?.let { catMap[it]?.name },
            lentAt = lent_at,
        )
    }
}