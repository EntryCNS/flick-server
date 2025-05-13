package com.flick.place.domain.order.service

import com.flick.common.error.CustomException
import com.flick.domain.order.entity.OrderEntity
import com.flick.domain.payment.entity.OrderItemEntity
import com.flick.domain.order.enums.OrderStatus
import com.flick.domain.payment.enums.ProductStatus
import com.flick.domain.order.error.OrderError
import com.flick.domain.payment.error.ProductError
import com.flick.domain.payment.repository.OrderItemRepository
import com.flick.domain.payment.repository.OrderRepository
import com.flick.domain.payment.repository.ProductRepository
import com.flick.place.domain.order.dto.request.CreateOrderRequest
import com.flick.place.domain.order.dto.response.OrderResponse
import com.flick.place.infra.security.SecurityHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val securityHolder: SecurityHolder,
    private val productRepository: ProductRepository,
    private val orderItemRepository: OrderItemRepository,
    private val transactionalOperator: TransactionalOperator
) {
    suspend fun getOrders(): Flow<OrderResponse> = transactionalOperator.executeAndAwait {
        val boothId = securityHolder.getBoothId()
        orderRepository.findAllByBoothId(boothId).map { it.toResponse() }
    }

    suspend fun getOrder(orderId: Long): OrderResponse = transactionalOperator.executeAndAwait {
        val order = orderRepository.findByIdAndBoothId(orderId, securityHolder.getBoothId())
            ?: throw CustomException(OrderError.ORDER_NOT_FOUND)

        order.toResponse()
    }

    suspend fun createOrder(request: CreateOrderRequest): OrderResponse = transactionalOperator.executeAndAwait {
        val boothId = securityHolder.getBoothId()

        val productIds = request.items.map { it.productId }
        val products = productRepository.findAllById(productIds).toList()

        if (products.size != request.items.distinct().size) {
            throw CustomException(ProductError.PRODUCT_NOT_FOUND)
        }

        val productMap = products.associateBy { it.id!! }

        request.items.forEach { item ->
            val product = productMap[item.productId] ?: throw CustomException(ProductError.PRODUCT_NOT_FOUND)

            if (product.boothId != boothId) {
                throw CustomException(ProductError.PRODUCT_NOT_FOUND)
            }

            if (product.status != ProductStatus.AVAILABLE) {
                throw CustomException(ProductError.PRODUCT_UNAVAILABLE)
            }

            if (product.stock < item.quantity) {
                throw CustomException(OrderError.INSUFFICIENT_STOCK)
            }
        }

        val totalAmount = request.items.sumOf { item ->
            val product = productMap[item.productId]!!
            product.price * item.quantity
        }

        val order = OrderEntity(
            boothId = boothId,
            userId = null,
            boothOrderNumber = 0,
            totalAmount = totalAmount,
            status = OrderStatus.PENDING,
            expiresAt = LocalDateTime.now().plusMinutes(15)
        )

        val savedOrder = orderRepository.save(order)

        val orderItems = request.items.map { item ->
            val product = productMap[item.productId]!!
            OrderItemEntity(
                orderId = savedOrder.id!!,
                productId = item.productId,
                productName = product.name,
                price = product.price,
                quantity = item.quantity
            )
        }

        orderItemRepository.saveAll(orderItems).collect()

        savedOrder.toResponse()
    }

    suspend fun cancelOrder(orderId: Long) = transactionalOperator.executeAndAwait {
        val order = orderRepository.findByIdAndBoothId(orderId, securityHolder.getBoothId())
            ?: throw CustomException(OrderError.ORDER_NOT_FOUND)

        if (order.status != OrderStatus.PENDING) {
            throw CustomException(OrderError.ORDER_NOT_PENDING)
        }

        orderRepository.save(order.copy(
            status = OrderStatus.CANCELED,
        ))
    }

    private fun OrderEntity.toResponse() = OrderResponse(
        id = id!!,
        userId = userId,
        boothOrderNumber = boothOrderNumber,
        totalAmount = totalAmount,
        status = status,
        expiresAt = expiresAt,
        paidAt = paidAt,
        completedAt = completedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}