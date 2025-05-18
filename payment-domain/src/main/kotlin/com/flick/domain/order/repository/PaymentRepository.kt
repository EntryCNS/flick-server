package com.flick.domain.order.repository

import com.flick.domain.order.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PaymentRepository : CoroutineCrudRepository<PaymentEntity, Long> {
    @Query("""
        SELECT p.* FROM payments p
        JOIN orders o ON p.order_id = o.id
        WHERE o.booth_id = :boothId
        ORDER BY p.created_at DESC
    """)
    fun findAllByBoothIdOrderByCreatedAtDesc(boothId: Long): Flow<PaymentEntity>
}