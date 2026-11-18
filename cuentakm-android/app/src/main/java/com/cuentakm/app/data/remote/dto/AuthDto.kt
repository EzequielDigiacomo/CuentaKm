package com.cuentakm.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(
    val idToken: String,
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val email: String,
    val name: String,
    val expiresAt: String? = null,
)
