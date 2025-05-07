package com.flick.domain.order.entity

import com.flick.domain.order.enums.OrderStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("orders")
data class OrderEntity(
    @Id
    val id: Long? = null,

    @Column("booth_id")
    val boothId: Long,

    @Column("user_id")
    val userId: Long? = null,

    @Column("booth_order_number")
    val boothOrderNumber: Int,

    @Column("total_amount")
    val totalAmount: Long,

    @Column("status")
    val status: OrderStatus = OrderStatus.PENDING,

    @Column("expires_at")
    val expiresAt: LocalDateTime,

    @Column("paid_at")
    val paidAt: LocalDateTime? = null,

    @Column("completed_at")
    val completedAt: LocalDateTime? = null,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)