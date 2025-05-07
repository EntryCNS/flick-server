package com.flick.place.infra.security

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JwtPayload(
    val accessToken: String,
    val refreshToken: String? = null,
)