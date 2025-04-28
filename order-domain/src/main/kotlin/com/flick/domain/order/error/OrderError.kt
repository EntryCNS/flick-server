package com.flick.domain.order.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class OrderError(override val status: HttpStatus, override val message: String): CustomError {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    BOOTH_MISMATCH(HttpStatus.FORBIDDEN, "해당 부스의 주문이 아닙니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 상태입니다."),
    EMPTY_ORDER_ITEMS(HttpStatus.BAD_REQUEST, "주문 항목이 비어있습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),
    PAYMENT_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "결제 요청 생성에 실패했습니다.")
}