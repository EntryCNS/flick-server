package com.flick.place.domain.product.controller

import com.flick.domain.product.enums.ProductStatus
import com.flick.place.domain.product.dto.CreateProductRequest
import com.flick.place.domain.product.dto.UpdateProductRequest
import com.flick.place.domain.product.service.ProductService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {
    @GetMapping
    suspend fun getProducts(@RequestParam status: ProductStatus? = null) =
        productService.getProducts(status)

    @GetMapping("/available")
    suspend fun getAvailableProducts() =
        productService.getAvailableProducts()

    @GetMapping("/{productId}")
    suspend fun getProduct(@PathVariable productId: Long) =
        productService.getProduct(productId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createProduct(@RequestBody request: CreateProductRequest) =
        productService.createProduct(request)

    @PutMapping("/{productId}")
    suspend fun updateProduct(
        @PathVariable productId: Long,
        @RequestBody request: UpdateProductRequest
    ) = productService.updateProduct(productId, request)

    @PatchMapping("/{productId}/status")
    suspend fun updateProductStatus(
        @PathVariable productId: Long,
        @RequestBody status: ProductStatus
    ) = productService.updateProductStatus(productId, status)

    @PatchMapping("/{productId}/stock")
    suspend fun updateProductStock(
        @PathVariable productId: Long,
        @RequestBody stock: Int
    ) = productService.updateProductStock(productId, stock)

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteProduct(@PathVariable productId: Long) = productService.deleteProduct(productId)
}