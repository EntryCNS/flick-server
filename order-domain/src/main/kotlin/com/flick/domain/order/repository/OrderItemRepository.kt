package com.flick.domain.payment.repository

import com.flick.domain.payment.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OrderItemRepository: CoroutineCrudRepository<OrderItemEntity, Long> {
    fun findAllByOrderId(orderId: Long): Flow<OrderItemEntity>
    suspend fun findFirstByOrderId(orderId: Long): OrderItemEntity?
}