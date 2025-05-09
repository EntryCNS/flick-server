package com.flick.admin.infra.dauth.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class DAuthError(override val status: HttpStatus, override val message: String) : CustomError {
    LOGIN_FAILED(HttpStatus.BAD_GATEWAY, "Failed to login with DAuth"),
    TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_GATEWAY, "Failed to exchange token"),
    REFRESH_FAILED(HttpStatus.BAD_GATEWAY, "Failed to refresh token"),
    USER_FETCH_FAILED(HttpStatus.BAD_GATEWAY, "Failed to fetch user info"),
}