package com.cuentakm.app.data.repository

import com.cuentakm.app.core.ApiConfig
import com.cuentakm.app.core.ApiResult
import com.cuentakm.app.core.apiCall
import com.cuentakm.app.core.notConfiguredFailure
import com.cuentakm.app.data.local.SessionStore
import com.cuentakm.app.data.remote.CuentaKmApi
import com.cuentakm.app.data.remote.dto.GoogleAuthRequest
import com.cuentakm.app.domain.model.AuthSession
import com.cuentakm.app.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: CuentaKmApi,
    private val sessionStore: SessionStore,
    private val apiConfig: ApiConfig,
) {
    val session: Flow<AuthSession?> = sessionStore.session

    suspend fun signInWithGoogleIdToken(idToken: String): ApiResult<AuthSession> {
        if (!apiConfig.isConfigured) return notConfiguredFailure()

        return when (val result = apiCall { api.signInWithGoogle(GoogleAuthRequest(idToken)) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> {
                val dto = result.value
                val session = AuthSession(
                    token = dto.token,
                    user = AuthUser(email = dto.email, name = dto.name),
                    expiresAt = dto.expiresAt,
                )
                sessionStore.save(session)
                ApiResult.Success(session)
            }
        }
    }

    suspend fun signOut() = sessionStore.clear()
}
