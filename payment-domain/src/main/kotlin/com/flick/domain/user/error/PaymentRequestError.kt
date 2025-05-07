package com.flick.domain.user.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class PaymentRequestError(override val status: HttpStatus, override val message: String): CustomError {
    PAYMENT_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "Payment request not found"),
    PAYMENT_REQUEST_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "Payment request already completed"),
    PAYMENT_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "Payment request failed"),
    PAYMENT_REQUEST_EXPIRED(HttpStatus.BAD_REQUEST, "Payment request expired"),
}