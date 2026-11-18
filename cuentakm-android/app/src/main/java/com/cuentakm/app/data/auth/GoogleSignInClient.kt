package com.cuentakm.app.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.cuentakm.app.core.ApiConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Obtiene un idToken de Google con Credential Manager (el reemplazo oficial de
 * GoogleSignIn, que está deprecado).
 *
 * El `serverClientId` debe ser el OAuth client de tipo **Web** — el mismo
 * `Google:ClientId` que valida el backend como `aud` del token.
 */
@Singleton
class GoogleSignInClient @Inject constructor(
    private val apiConfig: ApiConfig,
) {

    suspend fun idToken(context: Context): Result<String> {
        val clientId = apiConfig.googleWebClientId
        if (clientId.isBlank()) {
            return Result.failure(
                IllegalStateException(
                    "Falta configurar GOOGLE_WEB_CLIENT_ID en local.properties.",
                ),
            )
        }

        val option = GetGoogleIdOption.Builder()
            .setServerClientId(clientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val credentialManager = CredentialManager.create(context)
            val response: GetCredentialResponse =
                credentialManager.getCredential(context = context, request = request)
            val credential = response.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Result.success(googleCredential.idToken)
            } else {
                Result.failure(IllegalStateException("No se pudo obtener el token de Google."))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(IllegalStateException("Cancelaste el inicio de sesión."))
        } catch (e: NoCredentialException) {
            Result.failure(
                IllegalStateException(
                    "No hay cuentas de Google disponibles en el dispositivo. " +
                        "Agregá una en Ajustes y volvé a intentar.",
                ),
            )
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.message ?: "Falló el inicio de sesión con Google."))
        }
    }
}
