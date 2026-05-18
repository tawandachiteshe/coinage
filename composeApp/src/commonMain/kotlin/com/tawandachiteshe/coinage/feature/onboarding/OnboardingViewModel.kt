package com.tawandachiteshe.coinage.feature.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.UserPrefsRepository
import com.tawandachiteshe.coinage.data.UserProfileRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val profileRepo: UserProfileRepository,
    private val prefsRepo: UserPrefsRepository,
) : ViewModel() {

    var name by mutableStateOf("")
        private set

    fun onNameChange(value: String) { name = value }

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            profileRepo.saveName(name.trim().ifBlank { "You" })
            prefsRepo.setOnboardingDone()
            onDone()
        }
    }
}