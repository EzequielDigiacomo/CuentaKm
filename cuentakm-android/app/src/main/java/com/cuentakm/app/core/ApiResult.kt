package com.cuentakm.app.core

/**
 * Resultado de una llamada a la API. Nunca lanza: siempre informa, para que la
 * UI tenga un único camino de error.
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>

    data class Failure(
        val message: String,
        val status: Int? = null,
        val isNetworkError: Boolean = false,
    ) : ApiResult<Nothing>
}
