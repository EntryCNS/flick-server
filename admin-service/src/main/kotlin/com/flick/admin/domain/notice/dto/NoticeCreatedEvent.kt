package com.flick.admin.domain.notice.dto

data class NoticeCreatedEvent(
    val id: Long,
    val userId: Long,
    val title: String,
)