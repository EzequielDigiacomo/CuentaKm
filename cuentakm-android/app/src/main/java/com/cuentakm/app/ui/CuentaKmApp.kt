package com.cuentakm.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentakm.app.ui.components.LoadingBox
import com.cuentakm.app.ui.login.LoginScreen
import com.cuentakm.app.ui.navigation.CuentaKmNavHost

@Composable
fun CuentaKmApp(viewModel: AppViewModel = hiltViewModel()) {
    val state by viewModel.sessionState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val current = state) {
            SessionUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                LoadingBox()
            }

            SessionUiState.SignedOut -> LoginScreen()

            is SessionUiState.SignedIn -> CuentaKmNavHost(user = current.user)
        }
    }
}
