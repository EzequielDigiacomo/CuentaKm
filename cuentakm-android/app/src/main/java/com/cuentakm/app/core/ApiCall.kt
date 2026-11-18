package com.cuentakm.app.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/**
 * Espejo de `src/api/client.ts` de la web: el plan Free de Render duerme el
 * backend, así que el primer request del día puede fallar mientras "despierta".
 * Se reintenta una sola vez y sólo ante un fallo de red puro (sin respuesta HTTP).
 */
private const val MAX_NETWORK_RETRIES = 1
private const val NETWORK_RETRY_DELAY_MS = 1_500L

private val errorJson = Json { ignoreUnknownKeys = true }

@Serializable
private data class ApiErrorBody(val message: String? = null, val detail: String? = null)

suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> {
    var attempt = 0
    while (true) {
        try {
            return ApiResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            return ApiResult.Failure(message = httpMessage(e), status = e.code())
        } catch (e: IOException) {
            if (attempt >= MAX_NETWORK_RETRIES) {
                return ApiResult.Failure(
                    message = NETWORK_ERROR_MESSAGE,
                    isNetworkError = true,
                )
            }
            attempt++
            delay(NETWORK_RETRY_DELAY_MS)
        } catch (e: Exception) {
            return ApiResult.Failure(message = e.message ?: "Ocurrió un error inesperado.")
        }
    }
}

fun notConfiguredFailure(): ApiResult.Failure = ApiResult.Failure(
    message = "La app no tiene configurada la dirección del servidor (API_BASE_URL).",
)

private const val NETWORK_ERROR_MESSAGE =
    "No se pudo conectar con el servidor. Revisá tu conexión y, si el servicio estaba " +
        "apagado, puede tardar unos segundos en arrancar; intentá de nuevo."

private fun httpMessage(e: HttpException): String {
    val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
    val parsed = body?.let { runCatching { errorJson.decodeFromString<ApiErrorBody>(it) }.getOrNull() }
    parsed?.message?.takeIf { it.isNotBlank() }?.let { return it }

    return when (val code = e.code()) {
        401 -> "Tu sesión expiró. Volvé a iniciar sesión."
        403 -> "No tenés permisos para hacer esta operación."
        404 -> "No encontramos lo que buscabas."
        in 500..599 -> "El servidor tuvo un problema. Intentá de nuevo en unos minutos."
        else -> "La operación falló (HTTP $code)."
    }
}
