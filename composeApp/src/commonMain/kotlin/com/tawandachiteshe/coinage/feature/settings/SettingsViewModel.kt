package com.tawandachiteshe.coinage.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.CurrencyRepository
import com.tawandachiteshe.coinage.data.CurrencyRateService
import com.tawandachiteshe.coinage.data.UserPrefsRepository
import com.tawandachiteshe.coinage.data.backup.BackupOrchestrator
import com.tawandachiteshe.coinage.domain.DataError
import com.tawandachiteshe.coinage.domain.Result
import com.tawandachiteshe.coinage.domain.repository.BackupFileInfo
import com.tawandachiteshe.coinage.domain.repository.GoogleAuthRepository
import kotlinx.datetime.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val baseCurrencyCode: String = "USD",
    val isGoogleConnected: Boolean = false,
    val googleEmail: String? = null,
    val isSyncing: Boolean = false,
    val lastBackupInfo: BackupFileInfo? = null,
    val backupError: String? = null,
    val showRestoreConfirm: Boolean = false,
    val sheetUrl: String? = null,
    val isRefreshingRates: Boolean = false,
    val ratesLastUpdatedMs: Long? = null,
    val ratesError: String? = null,
)

sealed interface SettingsAction {
    data class OnCurrencyChange(val code: String) : SettingsAction
    data object OnBackupNow : SettingsAction
    data object OnRestoreFromDrive : SettingsAction
    data object OnConfirmRestore : SettingsAction
    data object DismissRestoreConfirm : SettingsAction
    data object OnSyncToSheets : SettingsAction
    data object DismissBackupError : SettingsAction
    data object DismissSheetUrl : SettingsAction
    data object RefreshGoogleState : SettingsAction
    data object OnRefreshRates : SettingsAction
    data object DismissRatesError : SettingsAction
}

class SettingsViewModel(
    private val currencyRepo: CurrencyRepository,
    private val userPrefsRepo: UserPrefsRepository,
    private val rateService: CurrencyRateService = CurrencyRateService(),
    private val googleAuthRepo: GoogleAuthRepository? = null,
    private val backupOrchestrator: BackupOrchestrator? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            currencyRepo.getBase().collect { base ->
                if (base != null) _state.update { it.copy(baseCurrencyCode = base.code) }
            }
        }
        refreshGoogleState()
        refreshBackupInfo()
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnCurrencyChange ->
                viewModelScope.launch {
                    currencyRepo.setBase(action.code)
                    userPrefsRepo.setBaseCurrency(action.code)
                }

            SettingsAction.OnBackupNow -> {
                if (backupOrchestrator == null) return
                _state.update { it.copy(isSyncing = true, backupError = null) }
                viewModelScope.launch {
                    when (val r = backupOrchestrator.backup()) {
                        is Result.Success -> refreshBackupInfo()
                        is Result.Error   -> _state.update { it.copy(backupError = r.error.toMessage()) }
                    }
                    _state.update { it.copy(isSyncing = false) }
                }
            }

            SettingsAction.OnRestoreFromDrive ->
                _state.update { it.copy(showRestoreConfirm = true) }

            SettingsAction.DismissRestoreConfirm ->
                _state.update { it.copy(showRestoreConfirm = false) }

            SettingsAction.OnConfirmRestore -> {
                if (backupOrchestrator == null) return
                _state.update { it.copy(isSyncing = true, showRestoreConfirm = false, backupError = null) }
                viewModelScope.launch {
                    when (val r = backupOrchestrator.restore()) {
                        is Result.Success -> { /* done */ }
                        is Result.Error   -> _state.update { it.copy(backupError = r.error.toMessage()) }
                    }
                    _state.update { it.copy(isSyncing = false) }
                }
            }

            SettingsAction.OnSyncToSheets -> {
                if (backupOrchestrator == null) return
                _state.update { it.copy(isSyncing = true, backupError = null) }
                viewModelScope.launch {
                    when (val r = backupOrchestrator.syncToSheets()) {
                        is Result.Success -> _state.update { it.copy(sheetUrl = r.data) }
                        is Result.Error   -> _state.update { it.copy(backupError = r.error.toMessage()) }
                    }
                    _state.update { it.copy(isSyncing = false) }
                }
            }

            SettingsAction.DismissBackupError ->
                _state.update { it.copy(backupError = null) }

            SettingsAction.DismissSheetUrl ->
                _state.update { it.copy(sheetUrl = null) }

            SettingsAction.RefreshGoogleState -> refreshGoogleState()

            SettingsAction.OnRefreshRates -> {
                _state.update { it.copy(isRefreshingRates = true, ratesError = null) }
                viewModelScope.launch {
                    try {
                        val rates = rateService.fetchRatesVsUsd()
                        rates.forEach { (code, rate) -> currencyRepo.updateRate(code, rate) }
                        @OptIn(kotlin.time.ExperimentalTime::class)
                        val now = Clock.System.now().toEpochMilliseconds()
                        _state.update { it.copy(ratesLastUpdatedMs = now) }
                    } catch (e: Exception) {
                        _state.update { it.copy(ratesError = "Could not fetch rates: ${e.message?.take(60)}") }
                    } finally {
                        _state.update { it.copy(isRefreshingRates = false) }
                    }
                }
            }

            SettingsAction.DismissRatesError ->
                _state.update { it.copy(ratesError = null) }
        }
    }

    fun refreshGoogleState() {
        viewModelScope.launch {
            val connected = googleAuthRepo?.isConnected() ?: false
            val email = if (connected && googleAuthRepo != null) googleAuthRepo.getConnectedEmail() else null
            _state.update { it.copy(isGoogleConnected = connected, googleEmail = email) }
        }
    }

    private fun refreshBackupInfo() {
        if (backupOrchestrator == null) return
        viewModelScope.launch {
            val info = backupOrchestrator.lastBackupInfo()
            _state.update { it.copy(lastBackupInfo = info) }
        }
    }
}

private fun DataError.Network.toMessage(): String = when (this) {
    DataError.Network.UNAUTHORIZED       -> "Not signed in to Google."
    DataError.Network.NOT_FOUND          -> "No backup found in Drive."
    DataError.Network.NO_INTERNET        -> "No internet connection."
    DataError.Network.SERVER_ERROR       -> "Google server error. Try again."
    DataError.Network.TOO_MANY_REQUESTS  -> "Too many requests. Wait a moment."
    else                                 -> "Something went wrong."
}