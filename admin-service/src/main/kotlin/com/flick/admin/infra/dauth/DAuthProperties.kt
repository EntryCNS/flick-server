package com.flick.admin.infra.dauth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "infra.dauth")
data class DAuthProperties @ConstructorBinding constructor(
    val clientId: String,
    val clientSecret: String,
    val redirectUrl: String,
)