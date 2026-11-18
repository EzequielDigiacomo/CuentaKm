package com.cuentakm.app.domain.model

import com.cuentakm.app.util.kmNumber
import com.cuentakm.app.util.monthsLabel
import java.time.LocalDate

enum class MaintenanceStatus(val label: String) {
    Ok("Al día"),
    Upcoming("Próximo"),
    Overdue("Vencido"),
    Unknown("Sin datos");

    companion object {
        fun fromApi(value: String): MaintenanceStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Unknown
    }
}

/**
 * Catálogo de servicios. Los intervalos por defecto son los mismos que
 * `MaintenanceDefaults` del backend y `MAINTENANCE_SERVICES` de la web.
 */
enum class MaintenanceType(
    val label: String,
    val defaultKmStep: Int,
    val defaultMonthsStep: Int,
) {
    Oil("Aceite", 10_000, 6),
    OilFilter("Filtro de aceite", 10_000, 6),
    FuelFilter("Filtro de nafta", 20_000, 12),
    CabinFilter("Filtro de habitáculo", 15_000, 12),
    Tires("Cubiertas", 40_000, 48),
    Belt("Correas", 60_000, 48),
    TransmissionFluid("Fluido de caja", 60_000, 24),
    Coolant("Líquido refrigerante", 40_000, 24);

    /** Por ejemplo: "10.000 km o 6 meses". */
    val intervalLabel: String
        get() = "${defaultKmStep.kmNumber()} km o ${monthsLabel(defaultMonthsStep)}"

    companion object {
        fun fromApi(value: String): MaintenanceType? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }

        /** Mismo orden que `MaintenanceDefaults.DisplayOrder` en el backend. */
        val displayOrder: List<MaintenanceType> = entries.toList()

        /** Chips principales del formulario (los filtros van bajo Aceite). */
        val primary: List<MaintenanceType> =
            listOf(Oil, Tires, Belt, TransmissionFluid, Coolant)

        val oilBundleFilters: List<MaintenanceType> =
            listOf(OilFilter, FuelFilter, CabinFilter)
    }
}

/** En Correas se registra qué correa se cambió dentro de la nota (igual que la web). */
enum class BeltKind(val chipLabel: String, val noteLabel: String) {
    Distribution("Distribución (motor)", "Correa de distribución"),
    PolyV("Poli-V (accesorios)", "Correa poli-V"),
}

data class MaintenanceSnapshot(
    val type: MaintenanceType,
    val lastDoneAt: LocalDate?,
    val lastKm: Int?,
    val nextDueAt: LocalDate?,
    val nextDueKm: Int?,
    val status: MaintenanceStatus,
    val kmRemaining: Int?,
    val daysRemaining: Int?,
)

data class VehicleSummary(
    val patente: String,
    val currentKm: Int,
    val services: List<MaintenanceSnapshot>,
)

data class MaintenanceRecord(
    val id: String,
    val type: MaintenanceType,
    val doneAt: LocalDate,
    val km: Int,
    val nextDueKm: Int,
    val nextDueAt: LocalDate,
    val notes: String?,
)
