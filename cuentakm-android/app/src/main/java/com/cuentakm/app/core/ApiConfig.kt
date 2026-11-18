package com.cuentakm.app.core

import com.cuentakm.app.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dirección del backend inyectada en build time (ver `app/build.gradle.kts`).
 * Si falta, la app lo informa en pantalla en lugar de apuntar a un host
 * equivocado en silencio.
 */
@Singleton
class ApiConfig @Inject constructor() {
    val baseUrl: String = BuildConfig.API_BASE_URL
    val isConfigured: Boolean = BuildConfig.API_BASE_URL.isNotBlank()
    val googleWebClientId: String = BuildConfig.GOOGLE_WEB_CLIENT_ID
}
