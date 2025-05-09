package com.flick.admin.infra.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "infra.jwt")
data class JwtProperties @ConstructorBinding constructor(
    val secret: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long,
)