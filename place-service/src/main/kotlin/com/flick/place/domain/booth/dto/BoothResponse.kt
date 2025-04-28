package com.flick.place.domain.booth.dto

import com.flick.domain.booth.entity.Booth

data class BoothResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val location: String?,
    val status: String
) {
    companion object {
        fun of(booth: Booth): BoothResponse {
            return BoothResponse(
                id = booth.id!!,
                name = booth.name,
                description = booth.description,
                location = booth.location,
                status = booth.status.name
            )
        }
    }
}