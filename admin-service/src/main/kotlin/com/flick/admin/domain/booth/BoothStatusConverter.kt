package com.flick.admin.domain.booth

import com.flick.common.error.CustomException
import com.flick.domain.booth.enums.BoothStatus
import com.flick.domain.booth.error.BoothError
import org.springframework.core.convert.converter.Converter

class BoothStatusConverter : Converter<String, BoothStatus> {
    override fun convert(source: String): BoothStatus {
        val status = BoothStatus.entries.find { it.name.equals(source, ignoreCase = true) }

        return status ?: throw CustomException(BoothError.BOOTH_INVALID_STATUS)
    }
}