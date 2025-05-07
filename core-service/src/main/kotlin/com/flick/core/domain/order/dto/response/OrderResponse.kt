package com.flick.core.domain.order.dto.response

data class OrderResponse(
    val id: Long,
    val booth: OrderBoothResponse,
    val items: List<OrderItemResponse>,
    val totalAmount: Long
) {
    data class OrderBoothResponse(
        val id: Long,
        val name: String,
    )

    data class OrderItemResponse(
        val product: OrderItemProductResponse,
        val price: Long,
        val quantity: Int,
    )

    data class OrderItemProductResponse(
        val id: Long,
        val name: String,
    )
}