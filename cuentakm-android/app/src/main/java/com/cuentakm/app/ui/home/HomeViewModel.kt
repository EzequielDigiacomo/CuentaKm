package com.cuentakm.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuentakm.app.data.repository.AuthRepository
import com.cuentakm.app.util.Patente
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val patente: String = "",
    val error: String? = null,
) {
    val isValid: Boolean get() = Patente.isValid(patente)
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun onPatenteChange(raw: String) {
        _state.update { it.copy(patente = Patente.filterInput(raw), error = null) }
    }

    /** Devuelve la patente normalizada, o null si no es válida (y avisa en la UI). */
    fun validatedPatente(): String? {
        val normalized = Patente.normalize(_state.value.patente)
        if (!Patente.isValid(normalized)) {
            _state.update {
                it.copy(error = "Ingresá una patente válida (entre 5 y 10 caracteres).")
            }
            return null
        }
        return normalized
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
