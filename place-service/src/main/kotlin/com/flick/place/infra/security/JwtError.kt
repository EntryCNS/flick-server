package com.flick.place.infra.security

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class JwtError(override val status: HttpStatus, override val message: String): CustomError {
    INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "Invalid token type"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid token"),
}