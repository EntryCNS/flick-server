package com.flick.domain.order.repository

import com.flick.domain.order.entity.OrderItem
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OrderItemRepository : CoroutineCrudRepository<OrderItem, Long> {
    fun findByOrderId(orderId: Long): Flow<OrderItem>
}