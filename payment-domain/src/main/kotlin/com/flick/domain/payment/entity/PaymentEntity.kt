package com.flick.domain.payment.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("payments")
data class PaymentEntity(
    @Id
    val id: Long? = null,

    @Column("order_id")
    val orderId: Long,

    @Column("request_id")
    val requestId: Long,

    @Column("user_id")
    val userId: Long,

    @Column("amount")
    val amount: Long,

    @Column("transaction_id")
    val transactionId: Long? = null,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)