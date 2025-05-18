package com.flick.core.domain.booth.dto.response

data class BoothResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val imageUrl: String?
)