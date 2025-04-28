package com.flick.place.domain.product.service

import com.flick.common.error.CustomException
import com.flick.domain.product.entity.Product
import com.flick.domain.product.enums.ProductStatus
import com.flick.domain.product.error.ProductError
import com.flick.domain.product.repository.ProductRepository
import com.flick.place.domain.product.dto.CreateProductRequest
import com.flick.place.domain.product.dto.ProductResponse
import com.flick.place.domain.product.dto.UpdateProductRequest
import com.flick.place.infra.security.JwtHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val jwtHolder: JwtHolder,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
) {
    suspend fun getProducts(status: ProductStatus? = null): Flow<ProductResponse> {
        val boothId = jwtHolder.getBoothId()
        val products = if (status != null) {
            productRepository.findByBoothIdAndStatus(boothId, status)
        } else {
            productRepository.findByBoothId(boothId)
        }

        return products.map { ProductResponse.of(it) }
    }

    suspend fun getProduct(id: Long): ProductResponse {
        val boothId = jwtHolder.getBoothId()
        val product = productRepository.findById(id) ?: throw CustomException(ProductError.PRODUCT_NOT_FOUND)

        if (product.boothId != boothId) {
            throw CustomException(ProductError.PRODUCT_BOOTH_MISMATCH)
        }

        return ProductResponse.of(product)
    }

    suspend fun createProduct(request: CreateProductRequest): ProductResponse {
        val boothId = jwtHolder.getBoothId()
        val product = Product(
            boothId = boothId,
            name = request.name,
            price = request.price,
            description = request.description,
            imageUrl = request.imageUrl,
            stock = request.stock,
            status = if (request.stock > 0) ProductStatus.AVAILABLE else ProductStatus.SOLD_OUT
        )
        val savedProduct = productRepository.save(product)
        return ProductResponse.of(savedProduct)
    }

    @Transactional
    suspend fun updateProduct(id: Long, request: UpdateProductRequest): ProductResponse {
        val boothId = jwtHolder.getBoothId()
        val product = productRepository.findById(id) ?: throw CustomException(ProductError.PRODUCT_NOT_FOUND)

        if (product.boothId != boothId) {
            throw CustomException(ProductError.PRODUCT_BOOTH_MISMATCH)
        }

        val updatedProduct = product.copy(
            name = request.name ?: product.name,
            price = request.price ?: product.price,
            description = request.description ?: product.description,
            imageUrl = request.imageUrl ?: product.imageUrl,
            stock = request.stock ?: product.stock,
            status = if (request.stock != null && request.stock <= 0) ProductStatus.SOLD_OUT else product.status,
        )

        val savedProduct = productRepository.save(updatedProduct)
        return ProductResponse.of(savedProduct)
    }

    @Transactional
    suspend fun updateProductStatus(id: Long, status: ProductStatus): ProductResponse {
        val boothId = jwtHolder.getBoothId()
        val product = productRepository.findById(id) ?: throw CustomException(ProductError.PRODUCT_NOT_FOUND)

        if (product.boothId != boothId) {
            throw CustomException(ProductError.PRODUCT_BOOTH_MISMATCH)
        }

        val updatedProduct = product.copy(
            status = status,
            updatedAt = LocalDateTime.now()
        )

        val savedProduct = productRepository.save(updatedProduct)
        return ProductResponse.of(savedProduct)
    }

    @Transactional
    suspend fun updateProductStock(id: Long, stock: Int): ProductResponse {
        if (stock < 0) {
            throw CustomException(ProductError.PRODUCT_INVALID_STOCK)
        }

        val boothId = jwtHolder.getBoothId()
        val product = productRepository.findById(id) ?: throw CustomException(ProductError.PRODUCT_NOT_FOUND)

        if (product.boothId != boothId) {
            throw CustomException(ProductError.PRODUCT_BOOTH_MISMATCH)
        }

        val status = if (stock <= 0) ProductStatus.SOLD_OUT else product.status

        val updatedProduct = product.copy(
            stock = stock,
            status = status,
        )

        val savedProduct = productRepository.save(updatedProduct)
        return ProductResponse.of(savedProduct)
    }

    @Transactional
    suspend fun deleteProduct(id: Long) {
        val boothId = jwtHolder.getBoothId()
        val product = productRepository.findById(id) ?: throw CustomException(ProductError.PRODUCT_NOT_FOUND)

        if (product.boothId != boothId) {
            throw CustomException(ProductError.PRODUCT_BOOTH_MISMATCH)
        }

        productRepository.deleteById(id)
    }

    suspend fun getAvailableProducts(): Flow<ProductResponse> {
        val boothId = jwtHolder.getBoothId()

        return productRepository.findAvailableByBoothId(boothId)
            .map { ProductResponse.of(it) }
    }
}