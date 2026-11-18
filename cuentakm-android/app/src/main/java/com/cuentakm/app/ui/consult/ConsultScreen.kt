package com.cuentakm.app.ui.consult

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentakm.app.domain.model.MaintenanceRecord
import com.cuentakm.app.domain.model.MaintenanceSnapshot
import com.cuentakm.app.ui.components.ErrorBanner
import com.cuentakm.app.ui.components.LoadingBox
import com.cuentakm.app.ui.components.StatusBadge
import com.cuentakm.app.util.asDisplay
import com.cuentakm.app.util.km
import com.cuentakm.app.util.remainingLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultScreen(
    onBack: () -> Unit,
    onLoadMaintenance: () -> Unit,
    viewModel: ConsultViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showKmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Cuando el PDF está listo se abre la hoja de compartir del sistema.
    LaunchedEffect(state.exportFile) {
        state.exportFile?.let { file ->
            context.startActivity(viewModel.shareIntent(file))
            viewModel.consumeExport()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.patente.ifBlank { "Vehículo" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Actualizar",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onLoadMaintenance,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Cargar cambio") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                KmCard(
                    patente = state.summary?.patente ?: state.patente,
                    currentKm = state.summary?.currentKm,
                    isSaving = state.isSavingKm,
                    onEdit = { showKmDialog = true },
                )
            }

            item {
                OutlinedButton(
                    onClick = viewModel::exportPdf,
                    enabled = state.summary != null && !state.isExporting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.size(10.dp))
                    }
                    Text(
                        text = if (state.isExporting) "Generando PDF…" else "Exportar PDF",
                    )
                }
            }

            state.error?.let { message ->
                item {
                    ErrorBanner(message = message, onDismiss = viewModel::dismissError)
                }
            }

            if (state.isLoading) {
                item { LoadingBox(text = "Consultando servicios…") }
            } else {
                item { SectionTitle("Estado por servicio") }

                val services = state.summary?.services.orEmpty()
                if (services.isEmpty()) {
                    item { EmptyText("Todavía no hay servicios cargados para esta patente.") }
                } else {
                    items(services, key = { it.type.name }) { snapshot ->
                        ServiceRow(snapshot)
                    }
                }

                item { SectionTitle("Historial") }

                if (state.history.isEmpty()) {
                    item { EmptyText("Todavía no registraste ningún cambio.") }
                } else {
                    items(state.history, key = { it.id }) { record ->
                        HistoryRow(
                            record = record,
                            isDeleting = state.deletingId == record.id,
                            onDelete = { viewModel.deleteRecord(record.id) },
                        )
                    }
                }
            }
        }
    }

    if (showKmDialog) {
        KmDialog(
            initialKm = state.summary?.currentKm?.toString().orEmpty(),
            isSaving = state.isSavingKm,
            onDismiss = { showKmDialog = false },
            onConfirm = { km ->
                showKmDialog = false
                viewModel.updateKm(km)
            },
        )
    }
}

@Composable
private fun KmCard(
    patente: String,
    currentKm: Int?,
    isSaving: Boolean,
    onEdit: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSaving, onClick = onEdit),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patente,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = currentKm?.km() ?: "Sin datos",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "Kilometraje actual · tocá para actualizar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.height(24.dp))
            } else {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Actualizar kilometraje",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ServiceRow(snapshot: MaintenanceSnapshot) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = snapshot.type.label,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                StatusBadge(snapshot.status)
            }

            Text(
                text = snapshot.type.intervalLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = remainingLabel(snapshot.kmRemaining, snapshot.daysRemaining),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            val next = snapshot.nextDueAt?.asDisplay()
            val nextKm = snapshot.nextDueKm?.km()
            if (next != null || nextKm != null) {
                Text(
                    text = "Próximo: " + listOfNotNull(next, nextKm).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(
    record: MaintenanceRecord,
    isDeleting: Boolean,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.type.label} · ${record.doneAt.asDisplay()}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = record.km.km(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                record.notes?.let { notes ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (isDeleting) {
                CircularProgressIndicator(modifier = Modifier.padding(12.dp).height(20.dp))
            } else {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar registro",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun EmptyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun KmDialog(
    initialKm: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var kmText by remember { mutableStateOf(initialKm) }
    val parsed = kmText.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Actualizar kilometraje") },
        text = {
            OutlinedTextField(
                value = kmText,
                onValueChange = { kmText = it.filter(Char::isDigit).take(7) },
                label = { Text("Kilometraje") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { parsed?.let(onConfirm) },
                enabled = parsed != null && !isSaving,
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
