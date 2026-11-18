package com.cuentakm.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuentakm.app.data.repository.AuthRepository
import com.cuentakm.app.domain.model.AuthUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface SessionUiState {
    data object Loading : SessionUiState
    data object SignedOut : SessionUiState
    data class SignedIn(val user: AuthUser) : SessionUiState
}

/**
 * Puerta de entrada de la UI: la sesión sale de DataStore, así que la app
 * "recuerda" el login entre aperturas sin lógica extra en las pantallas.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    val sessionState: StateFlow<SessionUiState> = authRepository.session
        .map { session ->
            if (session == null) SessionUiState.SignedOut
            else SessionUiState.SignedIn(session.user)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionUiState.Loading,
        )
}
