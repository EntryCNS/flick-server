package com.flick.place.infra.security

data class JwtPayload(
    val id: Long,
    val type: JwtType,
    val sessionId: Long? = null
)