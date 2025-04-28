package com.flick.place.domain.product.dto

import com.flick.domain.product.enums.ProductStatus

data class UpdateProductStatusRequest(
    val status: ProductStatus
)