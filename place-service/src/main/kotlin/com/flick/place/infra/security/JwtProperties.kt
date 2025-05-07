package com.flick.place.infra.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "infra.jwt")
data class JwtProperties @ConstructorBinding constructor(
    val booth: BoothJwtProperties,
    val kiosk: KioskJwtProperties,
) {
    data class BoothJwtProperties(
        val secret: String,
        val accessTokenExpiration: Long,
        val refreshTokenExpiration: Long,
    )

    data class KioskJwtProperties(
        val secret: String,
        val accessTokenExpiration: Long,
    )
}