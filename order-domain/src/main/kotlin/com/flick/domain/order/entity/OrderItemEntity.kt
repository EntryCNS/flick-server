package com.flick.domain.order.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("order_items")
data class OrderItemEntity(
    @Id
    val id: Long? = null,

    @Column("order_id")
    val orderId: Long,

    @Column("product_id")
    val productId: Long,

    @Column("product_name")
    val productName: String,

    @Column("price")
    val price: Long,

    @Column("quantity")
    val quantity: Int,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)