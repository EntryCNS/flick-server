package com.flick.domain.product.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class ProductError(override val status: HttpStatus, override val message: String) : CustomError {
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product not found"),
    PRODUCT_UNAVAILABLE(HttpStatus.BAD_REQUEST, "Product unavailable"),

}