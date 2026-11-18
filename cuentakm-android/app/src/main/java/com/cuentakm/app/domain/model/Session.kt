package com.cuentakm.app.domain.model

data class AuthUser(
    val email: String,
    val name: String,
)

data class AuthSession(
    val token: String,
    val user: AuthUser,
    val expiresAt: String? = null,
)
