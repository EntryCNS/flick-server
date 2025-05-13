package com.flick.admin.domain.notice.dto.request

data class CreateNoticeRequest(
    val title: String,
    val content: String,
    val isPinned: Boolean
)