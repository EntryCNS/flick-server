package com.flick.place.domain.product.dto.request

import com.flick.domain.product.enums.ProductStatus

data class UpdateProductRequest(
    val name: String?,
    val price: Long?,
    val description: String?,
    val imageUrl: String?,
    val stock: Int?,
    val status: ProductStatus?,
    val sortOrder: Int?
)