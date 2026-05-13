package com.tawandachiteshe.coinage.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tawandachiteshe.coinage.data.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val initial: String = "?",
)

class ProfileViewModel(
    private val profileRepo: UserProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val name = profileRepo.getName()?.ifBlank { null } ?: "You"
            _state.value = ProfileUiState(
                name = name,
                initial = name.first().uppercaseChar().toString(),
            )
        }
    }
}