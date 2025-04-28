package com.flick.domain.booth.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class BoothError(override val status: HttpStatus, override val message: String): CustomError {
    BOOTH_NOT_FOUND(HttpStatus.NOT_FOUND, "부스를 찾을 수 없습니다."),
    BOOTH_LOGIN_ID_DUPLICATED(HttpStatus.BAD_REQUEST, "이미 사용 중인 로그인 ID입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
    BOOTH_INACTIVE(HttpStatus.FORBIDDEN, "비활성화된 부스입니다."),
    BOOTH_PENDING(HttpStatus.UNAUTHORIZED, "승인 대기 중인 부스입니다."),
    BOOTH_REJECTED(HttpStatus.UNAUTHORIZED, "승인이 거부된 부스입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST , "유효하지 않은 리프레시 토큰입니다.")
}