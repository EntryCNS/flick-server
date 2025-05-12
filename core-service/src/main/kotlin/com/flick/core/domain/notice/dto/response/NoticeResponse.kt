package com.flick.core.domain.notice.dto.response

import java.time.LocalDateTime

data class NoticeResponse(
    val id: Long,
    val title: String,
    val content: String,
    val isPinned: Boolean,
    val createdAt: LocalDateTime,
)