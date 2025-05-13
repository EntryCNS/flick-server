package com.flick.notification.dto

data class NoticeCreatedEvent(
    val id: Long,
    val userId: Long,
    val title: String
)