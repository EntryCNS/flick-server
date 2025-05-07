package com.flick.place.domain.order.dto.request

data class CreateOrderRequest(
    val items: List<CreateOrderItemRequest>,
) {
    data class CreateOrderItemRequest(
        val productId: Long,
        val quantity: Int,
    )
}