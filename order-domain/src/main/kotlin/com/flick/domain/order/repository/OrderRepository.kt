package com.flick.domain.order.repository

import com.flick.domain.order.entity.OrderEntity
import com.flick.domain.order.enums.OrderStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OrderRepository : CoroutineCrudRepository<OrderEntity, Long> {
    fun findAllByBoothId(boothId: Long): Flow<OrderEntity>
    suspend fun findByIdAndBoothId(id: Long, boothId: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE status = 'PAID' ORDER BY booth_id, paid_at DESC")
    fun findByStatusPaid(): Flow<OrderEntity>

    fun findByBoothIdAndStatusOrderByPaidAtDesc(boothId: Long, status: OrderStatus): Flow<OrderEntity>
}