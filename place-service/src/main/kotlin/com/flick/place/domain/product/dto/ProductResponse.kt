package com.flick.place.domain.product.dto

import com.flick.domain.product.entity.Product
import com.flick.domain.product.enums.ProductStatus


data class ProductResponse(
    val id: Long,
    val name: String,
    val price: Long,
    val description: String?,
    val imageUrl: String?,
    val status: ProductStatus,
    val stock: Int
) {
    companion object {
        fun of(product: Product): ProductResponse {
            return ProductResponse(
                id = product.id!!,
                name = product.name,
                price = product.price,
                description = product.description,
                imageUrl = product.imageUrl,
                status = product.status,
                stock = product.stock
            )
        }
    }
}