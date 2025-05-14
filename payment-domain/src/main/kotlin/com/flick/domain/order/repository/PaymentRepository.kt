package com.flick.domain.order.repository

import com.flick.domain.order.entity.PaymentEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PaymentRepository : CoroutineCrudRepository<PaymentEntity, Long> {
}