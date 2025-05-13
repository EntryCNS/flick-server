package com.flick.admin.domain.notice.dto.response

import java.time.LocalDateTime

data class NoticeResponse(
    val id: Long,
    val title: String,
    val content: String,
    val isPinned: Boolean,
    val author: Author,
    val createdAt: LocalDateTime,
) {
    data class Author(
        val name: String
    )
}