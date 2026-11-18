package com.cuentakm.app.data.mapper

import com.cuentakm.app.data.remote.dto.MaintenanceDto
import com.cuentakm.app.data.remote.dto.MaintenanceSnapshotDto
import com.cuentakm.app.data.remote.dto.VehicleSummaryDto
import com.cuentakm.app.domain.model.MaintenanceRecord
import com.cuentakm.app.domain.model.MaintenanceSnapshot
import com.cuentakm.app.domain.model.MaintenanceStatus
import com.cuentakm.app.domain.model.MaintenanceType
import com.cuentakm.app.domain.model.VehicleSummary
import java.time.LocalDate

fun VehicleSummaryDto.toDomain(): VehicleSummary = VehicleSummary(
    patente = patente,
    currentKm = currentKm,
    // Los servicios de un tipo que la app todavía no conoce se descartan:
    // el backend puede agregar tipos nuevos sin romper esta versión.
    services = services.mapNotNull { it.toDomainOrNull() },
)

fun MaintenanceSnapshotDto.toDomainOrNull(): MaintenanceSnapshot? {
    val mappedType = MaintenanceType.fromApi(type) ?: return null
    return MaintenanceSnapshot(
        type = mappedType,
        lastDoneAt = lastDoneAt.toLocalDateOrNull(),
        lastKm = lastKm,
        nextDueAt = nextDueAt.toLocalDateOrNull(),
        nextDueKm = nextDueKm,
        status = MaintenanceStatus.fromApi(status),
        kmRemaining = kmRemaining,
        daysRemaining = daysRemaining,
    )
}

fun MaintenanceDto.toDomainOrNull(): MaintenanceRecord? {
    val mappedType = MaintenanceType.fromApi(type) ?: return null
    val done = doneAt.toLocalDateOrNull() ?: return null
    val next = nextDueAt.toLocalDateOrNull() ?: return null
    return MaintenanceRecord(
        id = id,
        type = mappedType,
        doneAt = done,
        km = km,
        nextDueKm = nextDueKm,
        nextDueAt = next,
        notes = notes?.takeIf { it.isNotBlank() },
    )
}

private fun String?.toLocalDateOrNull(): LocalDate? =
    this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
