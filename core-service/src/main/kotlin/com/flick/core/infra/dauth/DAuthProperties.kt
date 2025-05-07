package com.flick.core.infra.dauth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "infra.dauth")
data class DAuthProperties(
    val clientId: String,
    val clientSecret: String,
    val redirectUrl: String,
)