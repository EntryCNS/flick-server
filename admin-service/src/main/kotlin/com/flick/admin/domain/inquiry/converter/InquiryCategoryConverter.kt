package com.flick.admin.domain.inquiry.converter

import com.flick.domain.inquiry.enums.InquiryCategory
import org.springframework.core.convert.converter.Converter

class InquiryCategoryConverter : Converter<String, InquiryCategory> {
    override fun convert(source: String): InquiryCategory = InquiryCategory.valueOf(source.uppercase())
}