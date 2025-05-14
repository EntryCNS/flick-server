package com.flick.domain.order.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class OrderItemError(override val status: HttpStatus, override val message: String) : CustomError {
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Order item not found"),
}