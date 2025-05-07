package com.flick.domain.payment.entity

import com.flick.domain.payment.enums.ProductStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("products")
data class ProductEntity(
    @Id
    val id: Long? = null,

    @Column("booth_id")
    val boothId: Long,

    @Column("name")
    val name: String,

    @Column("price")
    val price: Long,

    @Column("description")
    val description: String? = null,

    @Column("image_url")
    val imageUrl: String? = null,

    @Column("stock")
    val stock: Int = 0,

    @Column("status")
    val status: ProductStatus = ProductStatus.AVAILABLE,

    @Column("sort_order")
    val sortOrder: Int = 0,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)