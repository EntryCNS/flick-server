package com.flick.domain.order.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class OrderError(override val status: HttpStatus, override val message: String) : CustomError {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order not found"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "Insufficient stock"),
    ORDER_NOT_PENDING(HttpStatus.BAD_REQUEST, "Order is not pending"),
}