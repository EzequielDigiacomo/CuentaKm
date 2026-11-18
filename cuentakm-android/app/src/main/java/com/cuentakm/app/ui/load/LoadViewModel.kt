package com.cuentakm.app.ui.load

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuentakm.app.core.ApiResult
import com.cuentakm.app.data.repository.VehicleRepository
import com.cuentakm.app.domain.model.BeltKind
import com.cuentakm.app.domain.model.MaintenanceType
import com.cuentakm.app.ui.navigation.Routes
import com.cuentakm.app.util.Patente
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/** Borrador de un filtro que acompaña al cambio de aceite. */
data class OilFilterDraft(
    val enabled: Boolean = false,
    val nextDueKm: String = "",
    val nextDueAt: String = "",
)

fun emptyOilFilters(): Map<MaintenanceType, OilFilterDraft> =
    MaintenanceType.oilBundleFilters.associateWith { OilFilterDraft() }

data class LoadUiState(
    val patente: String = "",
    val type: MaintenanceType = MaintenanceType.Oil,
    val beltKind: BeltKind = BeltKind.Distribution,
    val doneAt: String = LocalDate.now().toString(),
    val km: String = "",
    val nextDueKm: String = "",
    val nextDueAt: String = "",
    val notes: String = "",
    val oilFilters: Map<MaintenanceType, OilFilterDraft> = emptyOilFilters(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

private data class MaintenanceRequest(
    val type: MaintenanceType,
    val nextDueKm: Int,
    val nextDueAt: LocalDate,
    val notes: String?,
)

@HiltViewModel
class LoadViewModel @Inject constructor(
    private val vehicles: VehicleRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val patente: String =
        Patente.normalize(savedStateHandle.get<String>(Routes.PATENTE_ARG).orEmpty())

    private val _state = MutableStateFlow(LoadUiState(patente = patente))
    val state: StateFlow<LoadUiState> = _state.asStateFlow()

    fun onTypeChange(type: MaintenanceType) = _state.update { current ->
        current.copy(
            type = type,
            // Igual que la web: los filtros sólo existen dentro del cambio de aceite.
            oilFilters = if (type == MaintenanceType.Oil) current.oilFilters else emptyOilFilters(),
            error = null,
        )
    }

    fun onBeltKindChange(kind: BeltKind) = _state.update { it.copy(beltKind = kind, error = null) }

    fun onDoneAtChange(value: String) = _state.update { it.copy(doneAt = value, error = null) }

    fun onKmChange(value: String) =
        _state.update { it.copy(km = value.filter(Char::isDigit).take(7), error = null) }

    fun onNextDueKmChange(value: String) =
        _state.update { it.copy(nextDueKm = value.filter(Char::isDigit).take(7), error = null) }

    fun onNextDueAtChange(value: String) =
        _state.update { it.copy(nextDueAt = value, error = null) }

    fun onNotesChange(value: String) = _state.update { it.copy(notes = value, error = null) }

    fun toggleOilFilter(type: MaintenanceType, enabled: Boolean) = _state.update { current ->
        val draft = current.oilFilters[type] ?: OilFilterDraft()
        current.copy(
            oilFilters = current.oilFilters + (type to draft.copy(enabled = enabled)),
            error = null,
        )
    }

    fun onOilFilterNextKmChange(type: MaintenanceType, value: String) = _state.update { current ->
        val draft = current.oilFilters[type] ?: OilFilterDraft()
        current.copy(
            oilFilters = current.oilFilters +
                (type to draft.copy(nextDueKm = value.filter(Char::isDigit).take(7))),
            error = null,
        )
    }

    fun onOilFilterNextDateChange(type: MaintenanceType, value: String) = _state.update { current ->
        val draft = current.oilFilters[type] ?: OilFilterDraft()
        current.copy(
            oilFilters = current.oilFilters + (type to draft.copy(nextDueAt = value)),
            error = null,
        )
    }

    fun dismissError() = _state.update { it.copy(error = null) }

    fun submit() {
        val current = _state.value
        if (current.isSaving) return

        val doneAt = parseDate(current.doneAt)
        if (doneAt == null) {
            _state.update { it.copy(error = "Fecha inválida. Usá AAAA-MM-DD o DD/MM/AAAA.") }
            return
        }

        val km = current.km.toIntOrNull()
        if (km == null || km < 0) {
            _state.update { it.copy(error = "Ingresá un kilometraje válido.") }
            return
        }

        // Si el usuario no completa el próximo cambio, se aplican los intervalos
        // por defecto del servicio (los mismos que usa la web).
        val nextDueKm = current.nextDueKm.toIntOrNull() ?: (km + current.type.defaultKmStep)
        val nextDueAt = parseDate(current.nextDueAt)
            ?: doneAt.plusMonths(current.type.defaultMonthsStep.toLong())

        // Un POST por servicio: el principal y, si es aceite, uno por cada filtro marcado.
        // Todos comparten fecha y km, igual que en la web.
        val requests = buildList {
            add(
                MaintenanceRequest(
                    type = current.type,
                    nextDueKm = nextDueKm,
                    nextDueAt = nextDueAt,
                    notes = notesWithBelt(current),
                ),
            )

            if (current.type == MaintenanceType.Oil) {
                for (filter in MaintenanceType.oilBundleFilters) {
                    val draft = current.oilFilters[filter] ?: continue
                    if (!draft.enabled) continue

                    add(
                        MaintenanceRequest(
                            type = filter,
                            nextDueKm = draft.nextDueKm.toIntOrNull()
                                ?: (km + filter.defaultKmStep),
                            nextDueAt = parseDate(draft.nextDueAt)
                                ?: doneAt.plusMonths(filter.defaultMonthsStep.toLong()),
                            notes = null,
                        ),
                    )
                }
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            var failure: String? = null
            for (request in requests) {
                val result = vehicles.addMaintenance(
                    patente = patente,
                    type = request.type,
                    doneAt = doneAt,
                    km = km,
                    nextDueKm = request.nextDueKm,
                    nextDueAt = request.nextDueAt,
                    notes = request.notes,
                )
                if (result is ApiResult.Failure) {
                    failure = result.message
                    break
                }
            }

            _state.update { it.copy(isSaving = false, saved = failure == null, error = failure) }
        }
    }

    /** En Correas, la web guarda qué correa se cambió dentro de la nota del historial. */
    private fun notesWithBelt(current: LoadUiState): String? {
        val notes = current.notes.trim()
        if (current.type != MaintenanceType.Belt) return notes.ifBlank { null }

        val prefix = current.beltKind.noteLabel
        if (notes.contains("correa", ignoreCase = true)) return notes

        val value = if (notes.isEmpty()) prefix else "$prefix — $notes"
        return value.ifBlank { null }
    }

    private fun parseDate(raw: String): LocalDate? {
        val value = raw.trim()
        if (value.isEmpty()) return null

        return runCatching { LocalDate.parse(value) }.getOrNull()
            ?: runCatching { LocalDate.parse(value, ddMmYyyy) }.getOrNull()
    }

    private companion object {
        val ddMmYyyy: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }
}
