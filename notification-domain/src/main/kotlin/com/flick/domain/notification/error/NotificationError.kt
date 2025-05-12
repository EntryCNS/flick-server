package com.flick.domain.notification.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class NotificationError(override val status: HttpStatus, override val message: String): CustomError {
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),
}