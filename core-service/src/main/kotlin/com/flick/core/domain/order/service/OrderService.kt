package com.flick.core.domain.order.service

import com.flick.common.error.CustomException
import com.flick.core.domain.order.dto.response.OrderResponse
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.domain.order.enums.OrderStatus
import com.flick.domain.order.error.OrderError
import com.flick.domain.payment.repository.OrderItemRepository
import com.flick.domain.payment.repository.OrderRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val boothRepository: BoothRepository,
    private val orderItemRepository: OrderItemRepository,
    private val transactionalOperator: TransactionalOperator
) {
    suspend fun getOrder(orderId: Long): OrderResponse = transactionalOperator.executeAndAwait {
        val order = orderRepository.findById(orderId)
            ?: throw CustomException(OrderError.ORDER_NOT_FOUND)

        if (order.status != OrderStatus.PENDING)
            throw CustomException(OrderError.ORDER_NOT_PENDING)

        val booth = boothRepository.findById(order.boothId)
            ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)
        val items = orderItemRepository.findAllByOrderId(order.id!!).toList()

        OrderResponse(
            id = order.id!!,
            booth = OrderResponse.OrderBoothResponse(
                id = booth.id!!,
                name = booth.name,
            ),
            items = items.map {
                OrderResponse.OrderItemResponse(
                    product = OrderResponse.OrderItemProductResponse(
                        id = it.productId,
                        name = it.productName,
                    ),
                    price = it.price,
                    quantity = it.quantity,
                )
            },
            totalAmount = order.totalAmount
        )
    }
}