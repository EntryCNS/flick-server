package com.flick.admin.domain.inquiry.dto.response

import com.flick.domain.inquiry.enums.InquiryCategory
import java.time.LocalDateTime

data class InquiryInfoResponse(
    val id: Long,
    val category: InquiryCategory,
    val title: String,
    val content: String,
    val userId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
