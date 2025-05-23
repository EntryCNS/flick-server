package com.flick.domain.user.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class UserError(override val status: HttpStatus, override val message: String) : CustomError {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "Insufficient balance"),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "Permission denied"),

    INVALID_ROLE_TYPE(HttpStatus.BAD_REQUEST, "Invalid user role"),
    NOT_ENOUGH_BALANCE(HttpStatus.BAD_REQUEST, "Not enough balance"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "Invalid amount"),
    SELF_TRANSFER_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Self transfer not allowed"),
}