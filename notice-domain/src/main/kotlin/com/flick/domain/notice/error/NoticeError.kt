package com.flick.domain.notice.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class NoticeError(override val status: HttpStatus, override val message: String) : CustomError {
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."),
    NOTICE_FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
}