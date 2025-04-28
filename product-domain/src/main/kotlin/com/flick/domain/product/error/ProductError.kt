package com.flick.domain.product.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class ProductError(override val status: HttpStatus, override val message: String): CustomError {
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    PRODUCT_BOOTH_MISMATCH(HttpStatus.FORBIDDEN, "부스와 상품이 일치하지 않습니다."),
    PRODUCT_INVALID_STOCK(HttpStatus.BAD_REQUEST, "상품의 재고가 유효하지 않습니다."),
}