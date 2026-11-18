package com.cuentakm.app.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val esAr: Locale = Locale.forLanguageTag("es-AR")
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun Int.kmNumber(): String = NumberFormat.getIntegerInstance(esAr).format(this)

fun Int.km(): String = "${kmNumber()} km"

fun LocalDate.asDisplay(): String = format(dateFormatter)

fun monthsLabel(months: Int): String = when {
    months == 1 -> "1 mes"
    months % 12 == 0 -> {
        val years = months / 12
        if (years == 1) "1 año" else "$years años"
    }
    else -> "$months meses"
}

/** Por ejemplo: "faltan 1.200 km · 30 días". */
fun remainingLabel(kmRemaining: Int?, daysRemaining: Int?): String {
    if (kmRemaining == null && daysRemaining == null) {
        return "Todavía no hay un próximo cambio cargado."
    }

    val parts = buildList {
        if (kmRemaining != null) {
            add(
                if (kmRemaining >= 0) "faltan ${kmRemaining.kmNumber()} km"
                else "pasados ${(-kmRemaining).kmNumber()} km",
            )
        }
        if (daysRemaining != null) {
            add(
                if (daysRemaining >= 0) "$daysRemaining días"
                else "${-daysRemaining} días de atraso",
            )
        }
    }

    return parts.joinToString(" · ")
}
