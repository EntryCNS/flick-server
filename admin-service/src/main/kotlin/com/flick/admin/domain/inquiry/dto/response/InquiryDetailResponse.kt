package com.flick.admin.domain.inquiry.dto.response

import com.flick.domain.inquiry.enums.InquiryCategory
import java.time.LocalDateTime

data class InquiryDetailResponse(
    val id: Long,
    val category: InquiryCategory,
    val title: String,
    val content: String,
    val user: User,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    data class User(
        val id: Long,
        val name: String,
    )
}
