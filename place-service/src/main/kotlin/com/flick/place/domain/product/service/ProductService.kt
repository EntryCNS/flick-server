package com.flick.place.domain.product.service

import com.flick.common.error.CustomException
import com.flick.domain.product.entity.ProductEntity
import com.flick.domain.product.enums.ProductStatus
import com.flick.domain.product.error.ProductError
import com.flick.domain.product.repository.ProductRepository
import com.flick.place.domain.product.dto.request.CreateProductRequest
import com.flick.place.domain.product.dto.request.UpdateProductRequest
import com.flick.place.domain.product.dto.response.ProductResponse
import com.flick.place.infra.security.SecurityHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val securityHolder: SecurityHolder,
    private val transactionalOperator: TransactionalOperator
) {
    suspend fun getProducts(): Flow<ProductResponse> = transactionalOperator.executeAndAwait {
        val boothId = securityHolder.getBoothId()
        productRepository.findAllByBoothId(boothId).map { it.toResponse() }
    }

    suspend fun getAvailableProducts(): Flow<ProductResponse> = transactionalOperator.executeAndAwait {
        val boothId = securityHolder.getBoothId()
        productRepository.findAllByBoothIdAndStatus(boothId, ProductStatus.AVAILABLE)
            .map { it.toResponse() }
    }

    suspend fun getProduct(productId: Long): ProductResponse = transactionalOperator.executeAndAwait {
        val product = productRepository.findById(productId)
            ?: throw CustomException(ProductError.PRODUCT_NOT_FOUND)

        product.toResponse()
    }

    suspend fun createProduct(request: CreateProductRequest) = transactionalOperator.executeAndAwait {
        val boothId = securityHolder.getBoothId()

        productRepository.save(
            ProductEntity(
                boothId = boothId,
                name = request.name,
                price = request.price,
                description = request.description,
                imageUrl = request.imageUrl,
                stock = request.stock,
                status = ProductStatus.AVAILABLE,
                sortOrder = request.sortOrder
            )
        )
    }

    suspend fun updateProduct(productId: Long, request: UpdateProductRequest) = transactionalOperator.executeAndAwait {
        val product = productRepository.findById(productId)
            ?: throw CustomException(ProductError.PRODUCT_NOT_FOUND)

        productRepository.save(
            product.copy(
                name = request.name ?: product.name,
                price = request.price ?: product.price,
                stock = request.stock ?: product.stock,
                status = request.status ?: product.status,
                imageUrl = request.imageUrl ?: product.imageUrl,
                sortOrder = request.sortOrder ?: product.sortOrder,
                description = request.description ?: product.description
            )
        )
    }

    suspend fun deleteProduct(productId: Long) = transactionalOperator.executeAndAwait {
        val product = productRepository.findById(productId)
            ?: throw CustomException(ProductError.PRODUCT_NOT_FOUND)

        productRepository.delete(product)
    }

    private fun ProductEntity.toResponse() = ProductResponse(
        id = id!!,
        name = name,
        price = price,
        description = description,
        imageUrl = imageUrl,
        stock = stock,
        status = status,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}