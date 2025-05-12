package com.flick.core.domain.booth.dto.response

data class BoothDetailResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val products: List<Product>
) {
    data class Product(
        val id: Long,
        val name: String,
        val description: String?,
        val price: Long,
        val imageUrl: String?,
        val isSoldOut: Boolean,
    )
}