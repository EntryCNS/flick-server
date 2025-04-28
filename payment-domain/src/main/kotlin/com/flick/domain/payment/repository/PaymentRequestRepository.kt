package com.flick.domain.payment.repository

import com.flick.domain.payment.entity.PaymentRequest
import com.flick.domain.payment.enums.PaymentStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PaymentRequestRepository : CoroutineCrudRepository<PaymentRequest, Long> {
    fun findByOrderId(orderId: Long): Flow<PaymentRequest>
    suspend fun findByRequestCode(requestCode: String): PaymentRequest?

    @Query("SELECT * FROM payment_requests WHERE order_id = :orderId AND status = 'PENDING' ORDER BY created_at DESC LIMIT 1")
    suspend fun findLatestPendingByOrderId(orderId: Long): PaymentRequest?
}