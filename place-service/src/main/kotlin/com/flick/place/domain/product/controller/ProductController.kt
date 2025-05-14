package com.flick.place.domain.product.controller

import com.flick.place.domain.product.dto.request.CreateProductRequest
import com.flick.place.domain.product.dto.request.UpdateProductRequest
import com.flick.place.domain.product.dto.response.ProductResponse
import com.flick.place.domain.product.service.ProductService
import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {
    @GetMapping
    suspend fun getProducts() = productService.getProducts()

    @GetMapping("/available")
    suspend fun getAvailableProducts(): Flow<ProductResponse> = productService.getAvailableProducts()

    @GetMapping("/{productId}")
    suspend fun getProduct(@PathVariable productId: Long): ProductResponse = productService.getProduct(productId)

    @PostMapping
    suspend fun createProduct(@RequestBody request: CreateProductRequest) {
        productService.createProduct(request)
    }

    @PatchMapping("/{productId}")
    suspend fun updateProduct(
        @PathVariable productId: Long,
        @RequestBody request: UpdateProductRequest
    ) {
        productService.updateProduct(productId, request)
    }

    @DeleteMapping("/{productId}")
    suspend fun deleteProduct(@PathVariable productId: Long) = productService.deleteProduct(productId)
}