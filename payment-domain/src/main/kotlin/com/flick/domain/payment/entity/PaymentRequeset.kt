package com.flick.domain.payment.entity

import com.flick.domain.payment.enums.PaymentStatus
import com.flick.domain.payment.enums.RequestMethod
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("payment_requests")
data class PaymentRequest(
    @Id
    val id: Long? = null,

    @Column("order_id")
    val orderId: Long,

    @Column("request_code")
    val requestCode: String,

    @Column("request_method")
    val requestMethod: RequestMethod,

    @Column("status")
    val status: PaymentStatus = PaymentStatus.PENDING,

    @Column("user_id")
    val userId: Long? = null,

    @Column("student_id")
    val studentId: String? = null,

    @Column("payment_key")
    val paymentKey: String? = null,

    @Column("expires_at")
    val expiresAt: LocalDateTime = LocalDateTime.now().plusMinutes(10),

    @Column("completed_at")
    val completedAt: LocalDateTime? = null,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)