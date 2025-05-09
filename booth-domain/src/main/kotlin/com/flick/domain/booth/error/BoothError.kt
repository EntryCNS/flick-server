package com.flick.domain.booth.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class
BoothError(override val status: HttpStatus, override val message: String) : CustomError {
    BOOTH_NOT_FOUND(HttpStatus.NOT_FOUND, "Booth not found"),
    BOOTH_PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "Booth password does not match"),
    BOOTH_USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Booth username already exists"),

    BOOTH_NOT_APPROVED(HttpStatus.FORBIDDEN, "Booth not approved"),
    BOOTH_REJECTED(HttpStatus.FORBIDDEN, "Booth rejected"),
    BOOTH_INACTIVE(HttpStatus.FORBIDDEN, "Booth inactive"),

    BOOTH_INVALID_STATUS(HttpStatus.BAD_REQUEST, "Invalid booth status"),
}