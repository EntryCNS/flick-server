package com.flick.admin.domain.booth.converter

import com.flick.domain.booth.enums.BoothStatus
import org.springframework.core.convert.converter.Converter

class BoothStatusConverter : Converter<String, BoothStatus> {
    override fun convert(source: String): BoothStatus {
        return BoothStatus.valueOf(source.uppercase())
    }
}