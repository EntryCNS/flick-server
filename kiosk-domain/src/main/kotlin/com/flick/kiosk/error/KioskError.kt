package com.flick.kiosk.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class KioskError(override val status: HttpStatus, override val message: String): CustomError {
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "키오스크 세션을 찾을 수 없습니다."),
    SESSION_EXPIRED(HttpStatus.FORBIDDEN, "키오스크 세션이 만료되었습니다."),
    SESSION_INACTIVE(HttpStatus.BAD_REQUEST, "키오스크 세션이 비활성화되었습니다."),
    CONNECTION_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "연결 코드를 찾을 수 없습니다."),
    CONNECTION_CODE_EXPIRED(HttpStatus.UNAUTHORIZED, "연결 코드가 만료되었습니다."),
    BOOTH_MISMATCH(HttpStatus.FORBIDDEN, "해당 부스의 키오스크가 아닙니다.")
}