package com.cuentakm.app.ui.consult

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuentakm.app.core.ApiResult
import com.cuentakm.app.data.pdf.PdfExporter
import com.cuentakm.app.data.repository.VehicleRepository
import com.cuentakm.app.domain.model.MaintenanceRecord
import com.cuentakm.app.domain.model.VehicleSummary
import com.cuentakm.app.ui.navigation.Routes
import com.cuentakm.app.util.Patente
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ConsultUiState(
    val patente: String = "",
    val isLoading: Boolean = true,
    val isSavingKm: Boolean = false,
    val isExporting: Boolean = false,
    val deletingId: String? = null,
    val summary: VehicleSummary? = null,
    val history: List<MaintenanceRecord> = emptyList(),
    val error: String? = null,
    val exportFile: File? = null,
)

@HiltViewModel
class ConsultViewModel @Inject constructor(
    private val vehicles: VehicleRepository,
    private val pdfExporter: PdfExporter,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val patente: String =
        Patente.normalize(savedStateHandle.get<String>(Routes.PATENTE_ARG).orEmpty())

    private val _state = MutableStateFlow(ConsultUiState(patente = patente))
    val state: StateFlow<ConsultUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { load(showSpinner = true) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun updateKm(newKm: Int) {
        if (newKm < 0) {
            _state.update { it.copy(error = "El kilometraje no puede ser negativo.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSavingKm = true, error = null) }
            when (val result = vehicles.updateKm(patente, newKm)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isSavingKm = false, summary = result.value) }
                    // Los estados (Ok/Próximo/Vencido) se recalculan en el server.
                    load(showSpinner = false)
                }

                is ApiResult.Failure ->
                    _state.update { it.copy(isSavingKm = false, error = result.message) }
            }
        }
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(deletingId = id, error = null) }
            when (val result = vehicles.deleteMaintenance(patente, id)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            deletingId = null,
                            history = it.history.filterNot { record -> record.id == id },
                        )
                    }
                    load(showSpinner = false)
                }

                is ApiResult.Failure ->
                    _state.update { it.copy(deletingId = null, error = result.message) }
            }
        }
    }

    fun exportPdf() {
        val current = _state.value
        val summary = current.summary ?: return
        if (current.isExporting) return

        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, error = null) }
            try {
                val file = pdfExporter.export(summary)
                _state.update { it.copy(isExporting = false, exportFile = file) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isExporting = false,
                        error = e.message ?: "No se pudo generar el PDF.",
                    )
                }
            }
        }
    }

    fun shareIntent(file: File) = pdfExporter.shareIntent(file)

    fun consumeExport() = _state.update { it.copy(exportFile = null) }

    private suspend fun load(showSpinner: Boolean) {
        if (showSpinner) {
            _state.update { it.copy(isLoading = it.summary == null, error = null) }
        }

        val summaryResult = vehicles.summary(patente)
        val historyResult = vehicles.history(patente)

        val failure = (summaryResult as? ApiResult.Failure)
            ?: (historyResult as? ApiResult.Failure)

        _state.update { current ->
            current.copy(
                isLoading = false,
                summary = (summaryResult as? ApiResult.Success)?.value ?: current.summary,
                history = (historyResult as? ApiResult.Success)?.value ?: current.history,
                error = failure?.message ?: current.error,
            )
        }
    }
}
