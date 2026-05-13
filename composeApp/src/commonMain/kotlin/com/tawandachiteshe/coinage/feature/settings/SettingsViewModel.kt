package com.tawandachiteshe.coinage.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.CurrencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val baseCurrencyCode: String = "USD",
)

sealed interface SettingsAction {
    data class OnCurrencyChange(val code: String) : SettingsAction
}

class SettingsViewModel(
    private val currencyRepo: CurrencyRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            currencyRepo.getBase().collect { base ->
                if (base != null) _state.update { it.copy(baseCurrencyCode = base.code) }
            }
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnCurrencyChange ->
                viewModelScope.launch { currencyRepo.setBase(action.code) }
        }
    }
}