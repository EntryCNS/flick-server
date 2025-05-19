package com.flick.admin.domain.booth.dto.response

import com.flick.domain.product.enums.ProductStatus

data class BoothDetailResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val totalSales: Long,
    val products: List<Product>
) {
    data class Product(
        val id: Long,
        val name: String,
        val description: String?,
        val price: Long,
        val imageUrl: String?,
        val stock: Int,
        val status: ProductStatus,
        val sortOrder: Int
    )
}