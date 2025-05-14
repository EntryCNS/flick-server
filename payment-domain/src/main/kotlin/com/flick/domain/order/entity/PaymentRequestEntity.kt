package com.flick.domain.order.entity

import com.flick.domain.order.enums.PaymentMethod
import com.flick.domain.order.enums.PaymentStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime


@Table("payment_requests")
data class PaymentRequestEntity(
    @Id
    val id: Long? = null,

    @Column("order_id")
    val orderId: Long,

    @Column("token")
    val token: String,

    @Column("method")
    val method: PaymentMethod,

    @Column("status")
    val status: PaymentStatus = PaymentStatus.PENDING,

    @Column("user_id")
    val userId: Long? = null,

    @Column("expires_at")
    val expiresAt: LocalDateTime = LocalDateTime.now().plusMinutes(3),

    @Column("completed_at")
    val completedAt: LocalDateTime? = null,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)