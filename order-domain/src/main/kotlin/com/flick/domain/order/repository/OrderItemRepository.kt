package com.flick.domain.order.repository

import com.flick.domain.order.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OrderItemRepository : CoroutineCrudRepository<OrderItemEntity, Long> {
    fun findAllByOrderId(orderId: Long): Flow<OrderItemEntity>

    fun findByOrderId(orderId: Long): Flow<OrderItemEntity>

    @Query("""
        SELECT oi.* FROM order_items oi
        JOIN orders o ON oi.order_id = o.id
        WHERE o.status = 'PAID'
    """)
    fun findForPaidOrders(): Flow<OrderItemEntity>
}