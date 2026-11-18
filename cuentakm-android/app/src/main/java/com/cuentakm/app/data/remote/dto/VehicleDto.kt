package com.cuentakm.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Contratos de `CuentaKm.Api.Contracts`. Los enums viajan como string
 * (`JsonStringEnumConverter` en el backend) y las fechas como `yyyy-MM-dd`.
 */
@Serializable
data class UpdateKmRequestDto(
    val currentKm: Int,
)

@Serializable
data class VehicleSummaryDto(
    val patente: String,
    val currentKm: Int,
    val services: List<MaintenanceSnapshotDto> = emptyList(),
)

@Serializable
data class MaintenanceSnapshotDto(
    val type: String,
    val lastDoneAt: String? = null,
    val lastKm: Int? = null,
    val nextDueAt: String? = null,
    val nextDueKm: Int? = null,
    val status: String,
    val kmRemaining: Int? = null,
    val daysRemaining: Int? = null,
)

@Serializable
data class CreateMaintenanceRequestDto(
    val type: String,
    val doneAt: String,
    val km: Int,
    val nextDueKm: Int,
    val nextDueAt: String,
    val notes: String? = null,
)

@Serializable
data class MaintenanceDto(
    val id: String,
    val type: String,
    val doneAt: String,
    val km: Int,
    val nextDueKm: Int,
    val nextDueAt: String,
    val notes: String? = null,
)
