package com.flick.place.domain.product.dto

data class CreateProductRequest(
    val name: String,
    val price: Long,
    val description: String? = null,
    val imageUrl: String? = null,
    val stock: Int = 0
)