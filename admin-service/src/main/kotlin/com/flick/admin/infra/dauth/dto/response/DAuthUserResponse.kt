package com.flick.admin.infra.dauth.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DAuthUserResponse(
    val data: DAuthUser
)