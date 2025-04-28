package com.flick.domain.order.repository

import com.flick.domain.order.entity.Order
import com.flick.domain.order.enums.OrderStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDateTime

interface OrderRepository : CoroutineCrudRepository<Order, Long> {
    fun findByBoothId(boothId: Long): Flow<Order>
    fun findByBoothIdAndStatus(boothId: Long, status: OrderStatus): Flow<Order>
    suspend fun findByBoothIdAndBoothOrderNumber(boothId: Long, boothOrderNumber: Int): Order?

    @Query("SELECT * FROM orders WHERE booth_id = :boothId AND created_at >= :startDate AND created_at <= :endDate ORDER BY created_at DESC")
    fun findByBoothIdAndDateRange(boothId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Flow<Order>

    @Query("SELECT * FROM orders WHERE booth_id = :boothId AND created_at >= CURRENT_DATE ORDER BY created_at DESC")
    fun findTodayOrdersByBoothId(boothId: Long): Flow<Order>
}