package com.cuentakm.app.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuentakm.app.core.ApiConfig
import com.cuentakm.app.core.ApiResult
import com.cuentakm.app.data.auth.GoogleSignInClient
import com.cuentakm.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInClient: GoogleSignInClient,
    apiConfig: ApiConfig,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    /** Si falta la config, la pantalla lo avisa en vez de fallar al hacer click. */
    val isApiConfigured: Boolean = apiConfig.isConfigured

    fun signIn(context: Context) {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val tokenResult = googleSignInClient.idToken(context)
            val idToken = tokenResult.getOrNull()
            if (idToken == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = tokenResult.exceptionOrNull()?.message
                            ?: "No se pudo iniciar sesión con Google.",
                    )
                }
                return@launch
            }

            when (val result = authRepository.signInWithGoogleIdToken(idToken)) {
                is ApiResult.Failure ->
                    _state.update { it.copy(isLoading = false, error = result.message) }

                // La sesión cambia sola: el root observa el Flow de DataStore.
                is ApiResult.Success ->
                    _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}
