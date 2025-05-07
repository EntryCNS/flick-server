package com.flick.place.domain.product.dto.request

data class CreateProductRequest(
    val name: String,
    val price: Long,
    val description: String?,
    val imageUrl: String?,
    val stock: Int,
    val status: String,
    val sortOrder: Int
)