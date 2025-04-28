package com.flick.place.domain.order.dto

import com.flick.domain.order.entity.Order
import com.flick.domain.order.enums.OrderStatus
import com.flick.domain.payment.entity.PaymentRequest
import com.flick.domain.payment.enums.PaymentStatus
import com.flick.domain.payment.enums.RequestMethod
import com.flick.domain.product.entity.Product
import java.time.LocalDateTime

data class OrderItemRequest(
    val productId: Long,
    val quantity: Int
)

data class CreateOrderRequest(
    val items: List<OrderItemRequest>
)

data class OrderItemResponse(
    val id: Long,
    val productId: Long,
    val name: String,
    val price: Long,
    val quantity: Int
)

data class OrderResponse(
    val id: Long,
    val boothOrderNumber: Int,
    val totalAmount: Long,
    val status: OrderStatus,
    val items: List<OrderItemResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun of(order: Order, items: List<OrderItemResponse>) =
            OrderResponse(
                id = order.id!!,
                boothOrderNumber = order.boothOrderNumber,
                totalAmount = order.totalAmount,
                status = order.status,
                items = items,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
    }
}

data class CreatePaymentRequestDto(
    val orderId: Long,
    val method: RequestMethod,
    val studentId: String? = null
)

data class PaymentRequestResponse(
    val id: Long,
    val orderId: Long,
    val requestCode: String,
    val requestMethod: RequestMethod,
    val status: PaymentStatus,
    val expiresAt: LocalDateTime
) {
    companion object {
        fun of(paymentRequest: PaymentRequest) =
            PaymentRequestResponse(
                id = paymentRequest.id!!,
                orderId = paymentRequest.orderId,
                requestCode = paymentRequest.requestCode,
                requestMethod = paymentRequest.requestMethod,
                status = paymentRequest.status,
                expiresAt = paymentRequest.expiresAt
            )
    }
}

data class ProductSummaryResponse(
    val id: Long,
    val name: String,
    val price: Long
) {
    companion object {
        fun from(product: Product) =
            ProductSummaryResponse(
                id = product.id!!,
                name = product.name,
                price = product.price
            )
    }
}