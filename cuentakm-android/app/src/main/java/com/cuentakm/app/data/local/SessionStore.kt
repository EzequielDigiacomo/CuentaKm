package com.cuentakm.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cuentakm.app.domain.model.AuthSession
import com.cuentakm.app.domain.model.AuthUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/**
 * Sesión persistida en DataStore. El JWT se guarda acá y lo lee el interceptor
 * de OkHttp para no tener que pasarlo por cada llamada.
 *
 * Nota: es almacenamiento privado de la app. Si más adelante se quiere cifrar
 * en reposo, se puede envolver con EncryptedFile/Keystore sin tocar la API.
 */
@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.sessionDataStore

    val session: Flow<AuthSession?> = dataStore.data.map { prefs ->
        val token = prefs[Keys.token]
        val email = prefs[Keys.email]
        if (token.isNullOrBlank() || email.isNullOrBlank()) {
            null
        } else {
            AuthSession(
                token = token,
                user = AuthUser(email = email, name = prefs[Keys.name].orEmpty()),
                expiresAt = prefs[Keys.expiresAt],
            )
        }
    }

    suspend fun current(): AuthSession? = session.first()

    suspend fun save(session: AuthSession) {
        dataStore.edit { prefs ->
            prefs[Keys.token] = session.token
            prefs[Keys.email] = session.user.email
            prefs[Keys.name] = session.user.name
            session.expiresAt?.let { prefs[Keys.expiresAt] = it }
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private object Keys {
        val token = stringPreferencesKey("token")
        val email = stringPreferencesKey("email")
        val name = stringPreferencesKey("name")
        val expiresAt = stringPreferencesKey("expiresAt")
    }
}
