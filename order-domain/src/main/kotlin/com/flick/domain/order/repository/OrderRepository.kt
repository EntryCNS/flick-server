package com.flick.domain.payment.repository

import com.flick.domain.order.entity.OrderEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OrderRepository: CoroutineCrudRepository<OrderEntity, Long> {
    fun findAllByBoothId(boothId: Long): Flow<OrderEntity>
    suspend fun findByIdAndBoothId(id: Long, boothId: Long): OrderEntity?
}