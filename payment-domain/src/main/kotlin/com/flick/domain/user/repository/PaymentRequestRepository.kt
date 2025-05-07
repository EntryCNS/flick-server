package com.flick.domain.payment.repository

import com.flick.domain.payment.entity.PaymentRequestEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PaymentRequestRepository: CoroutineCrudRepository<PaymentRequestEntity, Long> {
    fun findAllByOrderId(orderId: Long): Flow<PaymentRequestEntity>
    suspend fun findByToken(token: String): PaymentRequestEntity?
}