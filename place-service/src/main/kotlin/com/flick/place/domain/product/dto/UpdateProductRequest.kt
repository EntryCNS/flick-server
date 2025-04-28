package com.flick.place.domain.product.dto

data class UpdateProductRequest(
    val name: String? = null,
    val price: Long? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val stock: Int? = null
)