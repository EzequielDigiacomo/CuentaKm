package com.cuentakm.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentakm.app.domain.model.AuthUser
import com.cuentakm.app.ui.components.ErrorBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: AuthUser,
    onConsult: (String) -> Unit,
    onLoad: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "CuentaKm") },
                actions = {
                    IconButton(onClick = viewModel::signOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = "Hola, ${user.name.substringBefore(' ').ifBlank { "bienvenido" }}",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Ingresá la patente para ver cómo viene cada servicio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = state.patente,
                        onValueChange = viewModel::onPatenteChange,
                        label = { Text("Patente") },
                        placeholder = { Text("AB123CD") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done,
                        ),
                        supportingText = {
                            Text("Sin espacios ni guiones. Ejemplo: AB123CD")
                        },
                        isError = state.error != null,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (state.error != null) {
                        ErrorBanner(message = state.error!!, onDismiss = viewModel::dismissError)
                    }

                    Button(
                        onClick = { viewModel.validatedPatente()?.let(onConsult) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Ver estado")
                    }

                    OutlinedButton(
                        onClick = { viewModel.validatedPatente()?.let(onLoad) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Cargar un cambio")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Si es la primera vez que cargás una patente, el vehículo se crea " +
                    "automáticamente con ese primer cambio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
