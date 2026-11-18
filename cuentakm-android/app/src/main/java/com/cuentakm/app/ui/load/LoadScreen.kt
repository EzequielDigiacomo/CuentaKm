package com.cuentakm.app.ui.load

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentakm.app.domain.model.BeltKind
import com.cuentakm.app.domain.model.MaintenanceType
import com.cuentakm.app.ui.components.ErrorBanner
import com.cuentakm.app.ui.components.ServiceChip
import com.cuentakm.app.ui.components.dashedBorder
import com.cuentakm.app.ui.components.visuals
import com.cuentakm.app.util.asDisplay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LoadScreen(
    onBack: () -> Unit,
    viewModel: LoadViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    val km = state.km.toIntOrNull()
    val doneAt = runCatching { LocalDate.parse(state.doneAt) }.getOrNull()
    val suggestedKm = km?.plus(state.type.defaultKmStep)?.toString().orEmpty()
    val suggestedDate = doneAt
        ?.plusMonths(state.type.defaultMonthsStep.toLong())
        ?.asDisplay()
        .orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar un cambio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        value = state.patente,
                        onValueChange = {},
                        label = { Text("Patente") },
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    FieldLabel("Tipo de cambio")

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MaintenanceType.primary.forEach { type ->
                            ServiceChip(
                                type = type,
                                selected = state.type == type,
                                onClick = { viewModel.onTypeChange(type) },
                            )
                        }
                    }

                    if (state.type == MaintenanceType.Belt) {
                        BeltBlock(
                            selected = state.beltKind,
                            onSelect = viewModel::onBeltKindChange,
                        )
                    }

                    Text(
                        text = "El próximo vencimiento se precarga: cada " +
                            "${state.type.intervalLabel}. Lo podés ajustar abajo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedTextField(
                        value = state.doneAt,
                        onValueChange = viewModel::onDoneAtChange,
                        label = { Text("Fecha del cambio") },
                        singleLine = true,
                        supportingText = { Text("Formato AAAA-MM-DD o DD/MM/AAAA") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = state.km,
                        onValueChange = viewModel::onKmChange,
                        label = { Text("Km al momento del cambio") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = state.nextDueKm.ifBlank { suggestedKm },
                        onValueChange = viewModel::onNextDueKmChange,
                        label = { Text("Próximo cambio (km)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            Text("Editable. Se precarga con el intervalo por defecto.")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = state.nextDueAt.ifBlank { suggestedDate },
                        onValueChange = viewModel::onNextDueAtChange,
                        label = { Text("Próximo cambio (fecha)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (state.type == MaintenanceType.Oil) {
                        OilFiltersBlock(
                            km = km,
                            doneAt = doneAt,
                            filters = state.oilFilters,
                            onToggle = viewModel::toggleOilFilter,
                            onNextKmChange = viewModel::onOilFilterNextKmChange,
                            onNextDateChange = viewModel::onOilFilterNextDateChange,
                        )
                    }

                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = viewModel::onNotesChange,
                        label = { Text("Notas (opcional)") },
                        placeholder = { Text("Marca, repuesto, taller…") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (state.error != null) {
                        ErrorBanner(message = state.error!!, onDismiss = viewModel::dismissError)
                    }

                    Button(
                        onClick = viewModel::submit,
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(Modifier.size(12.dp))
                        }
                        Text(
                            text = if (state.isSaving) "Guardando…" else "Guardar cambio",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BeltBlock(
    selected: BeltKind,
    onSelect: (BeltKind) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .dashedBorder(MaterialTheme.colorScheme.outline)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FieldLabel("¿Qué correa cambiaste?")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BeltKind.entries.forEach { kind ->
                BeltChip(
                    label = kind.chipLabel,
                    selected = selected == kind,
                    onClick = { onSelect(kind) },
                )
            }
        }

        Text(
            text = "Queda registrado junto con la fecha y el km de este cambio.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BeltChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(percent = 50)
    val accent = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
            )
            .border(
                width = 1.dp,
                color = if (selected) accent else MaterialTheme.colorScheme.outline,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun OilFiltersBlock(
    km: Int?,
    doneAt: LocalDate?,
    filters: Map<MaintenanceType, OilFilterDraft>,
    onToggle: (MaintenanceType, Boolean) -> Unit,
    onNextKmChange: (MaintenanceType, String) -> Unit,
    onNextDateChange: (MaintenanceType, String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .dashedBorder(MaterialTheme.colorScheme.outline)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FieldLabel("Filtros en este cambio de aceite")

        Text(
            text = "Marcá los que también reemplazaste. Heredan la fecha y el km de arriba; " +
                "el próximo vencimiento de cada filtro se calcula solo y lo podés ajustar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        MaintenanceType.oilBundleFilters.forEach { filter ->
            val draft = filters[filter] ?: OilFilterDraft()
            OilFilterCard(
                type = filter,
                draft = draft,
                km = km,
                doneAt = doneAt,
                onToggle = { enabled -> onToggle(filter, enabled) },
                onNextKmChange = { value -> onNextKmChange(filter, value) },
                onNextDateChange = { value -> onNextDateChange(filter, value) },
            )
        }
    }
}

@Composable
private fun OilFilterCard(
    type: MaintenanceType,
    draft: OilFilterDraft,
    km: Int?,
    doneAt: LocalDate?,
    onToggle: (Boolean) -> Unit,
    onNextKmChange: (String) -> Unit,
    onNextDateChange: (String) -> Unit,
) {
    val visuals = type.visuals()
    val suggestedKm = km?.plus(type.defaultKmStep)?.toString().orEmpty()
    val suggestedDate = doneAt?.plusMonths(type.defaultMonthsStep.toLong())?.asDisplay().orEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(visuals.accent),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(!draft.enabled) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = draft.enabled,
                    onCheckedChange = { onToggle(it) },
                )
                Icon(
                    imageVector = visuals.icon,
                    contentDescription = null,
                    tint = visuals.accent,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = type.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (draft.enabled) {
                OutlinedTextField(
                    value = draft.nextDueKm.ifBlank { suggestedKm },
                    onValueChange = onNextKmChange,
                    label = { Text("Próximo cambio (km)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Cada ${type.intervalLabel}.") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = draft.nextDueAt.ifBlank { suggestedDate },
                    onValueChange = onNextDateChange,
                    label = { Text("Próximo cambio (fecha)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
