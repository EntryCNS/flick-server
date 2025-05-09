package com.flick.domain.booth.enums

import com.flick.common.error.CustomException
import com.flick.domain.booth.error.BoothError

enum class BoothStatus(val code: String) {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    INACTIVE("inactive");

    companion object {
        fun fromCode(code: String): BoothStatus =
            entries.firstOrNull { it.code == code.lowercase() }
                ?: throw CustomException(BoothError.BOOTH_INVALID_STATUS)
    }
}