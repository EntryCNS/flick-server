package com.flick.place.domain.product.dto.response

import com.flick.domain.payment.enums.ProductStatus
import java.time.LocalDateTime

data class ProductResponse(
    val id: Long,
    val name: String,
    val price: Long,
    val description: String?,
    val imageUrl: String?,
    val stock: Int,
    val status: ProductStatus,
    val sortOrder: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)