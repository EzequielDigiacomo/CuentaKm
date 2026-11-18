package com.cuentakm.app.data.repository

import com.cuentakm.app.core.ApiConfig
import com.cuentakm.app.core.ApiResult
import com.cuentakm.app.core.apiCall
import com.cuentakm.app.core.notConfiguredFailure
import com.cuentakm.app.data.mapper.toDomain
import com.cuentakm.app.data.mapper.toDomainOrNull
import com.cuentakm.app.data.remote.CuentaKmApi
import com.cuentakm.app.data.remote.dto.CreateMaintenanceRequestDto
import com.cuentakm.app.data.remote.dto.UpdateKmRequestDto
import com.cuentakm.app.domain.model.MaintenanceRecord
import com.cuentakm.app.domain.model.MaintenanceType
import com.cuentakm.app.domain.model.VehicleSummary
import com.cuentakm.app.util.Patente
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepository @Inject constructor(
    private val api: CuentaKmApi,
    private val apiConfig: ApiConfig,
) {

    suspend fun summary(patente: String): ApiResult<VehicleSummary> {
        if (!apiConfig.isConfigured) return notConfiguredFailure()
        val normalized = Patente.normalize(patente)
        return apiCall { api.vehicleSummary(normalized).toDomain() }
    }

    suspend fun history(patente: String): ApiResult<List<MaintenanceRecord>> {
        if (!apiConfig.isConfigured) return notConfiguredFailure()
        val normalized = Patente.normalize(patente)
        return apiCall { api.maintenanceHistory(normalized).mapNotNull { it.toDomainOrNull() } }
    }

    suspend fun addMaintenance(
        patente: String,
        type: MaintenanceType,
        doneAt: LocalDate,
        km: Int,
        nextDueKm: Int,
        nextDueAt: LocalDate,
        notes: String?,
    ): ApiResult<MaintenanceRecord> {
        if (!apiConfig.isConfigured) return notConfiguredFailure()
        val normalized = Patente.normalize(patente)

        val body = CreateMaintenanceRequestDto(
            type = type.name,
            doneAt = doneAt.toString(),
            km = km,
            nextDueKm = nextDueKm,
            nextDueAt = nextDueAt.toString(),
            notes = notes?.takeIf { it.isNotBlank() },
        )

        return when (val result = apiCall { api.createMaintenance(normalized, body) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> result.value.toDomainOrNull()
                ?.let { ApiResult.Success(it) }
                ?: ApiResult.Failure("El servidor devolvió un registro que no pudimos interpretar.")
        }
    }

    suspend fun updateKm(patente: String, currentKm: Int): ApiResult<VehicleSummary> {
        if (!apiConfig.isConfigured) return notConfiguredFailure()
        val normalized = Patente.normalize(patente)
        return apiCall {
            api.updateKm(normalized, UpdateKmRequestDto(currentKm = currentKm)).toDomain()
        }
    }

    suspend fun deleteMaintenance(patente: String, id: String): ApiResult<Unit> {
        if (!apiConfig.isConfigured) return notConfiguredFailure()
        val normalized = Patente.normalize(patente)
        return apiCall { api.deleteMaintenance(normalized, id) }
    }
}
