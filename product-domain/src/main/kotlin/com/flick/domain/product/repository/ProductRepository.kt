package com.flick.domain.product.repository

import com.flick.domain.product.entity.Product
import com.flick.domain.product.enums.ProductStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProductRepository : CoroutineCrudRepository<Product, Long> {
    fun findByBoothId(boothId: Long): Flow<Product>
    fun findByBoothIdAndStatus(boothId: Long, status: ProductStatus): Flow<Product>

    @Query("SELECT * FROM products WHERE booth_id = :boothId AND stock > 0 AND status = 'AVAILABLE'")
    fun findAvailableByBoothId(boothId: Long): Flow<Product>
}