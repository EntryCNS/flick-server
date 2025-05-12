package com.flick.core.domain.inquiry.dto.request

import com.flick.domain.inquiry.enums.InquiryCategory

data class CreateInquiryRequest(
    val category: InquiryCategory,
    val title: String,
    val content: String,
)