package com.flick.common.error

import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val code: String,
    val message: String,
    val path: String
)