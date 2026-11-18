package com.cuentakm.app.data.remote

import com.cuentakm.app.data.local.SessionStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adjunta el JWT a cada request. Corre en un hilo de OkHttp (no en el main),
 * por eso la lectura puntual del DataStore con runBlocking es aceptable.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionStore: SessionStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header(HEADER) != null) {
            return chain.proceed(request)
        }

        val token = runBlocking { sessionStore.current()?.token }
        val authorized = if (token.isNullOrBlank()) {
            request
        } else {
            request.newBuilder().header(HEADER, "Bearer $token").build()
        }

        return chain.proceed(authorized)
    }

    private companion object {
        const val HEADER = "Authorization"
    }
}
