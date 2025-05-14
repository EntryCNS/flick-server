package com.flick.domain.payment.repository

import com.flick.domain.payment.entity.PaymentEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PaymentRepository : CoroutineCrudRepository<PaymentEntity, Long> {
}