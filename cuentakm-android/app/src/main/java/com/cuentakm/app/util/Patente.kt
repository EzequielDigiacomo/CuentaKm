package com.cuentakm.app.util

/**
 * Espejo de `PatenteNormalizer` del backend: mayúsculas y sólo A-Z / 0-9.
 * La app normaliza antes de enviar para que el usuario pueda escribir
 * "ab 123 cd" y obtenga el mismo resultado que en la web.
 */
object Patente {
    private val nonAlphanumeric = Regex("[^A-Z0-9]")

    fun normalize(raw: String): String =
        nonAlphanumeric.replace(raw.trim().uppercase(), "")

    fun isValid(raw: String): Boolean = normalize(raw).length in 5..10

    /** Filtro para ir escribiendo en el campo, sin dejar meter símbolos. */
    fun filterInput(raw: String): String =
        nonAlphanumeric.replace(raw.uppercase(), "").take(10)
}
