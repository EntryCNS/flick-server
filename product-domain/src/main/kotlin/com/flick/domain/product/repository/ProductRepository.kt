package com.flick.domain.product.repository

import com.flick.domain.product.entity.ProductEntity
import com.flick.domain.product.enums.ProductStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProductRepository : CoroutineCrudRepository<ProductEntity, Long> {
    fun findAllByBoothId(boothId: Long): Flow<ProductEntity>
    fun findAllByBoothIdAndStatusIn(boothId: Long, statuses: List<ProductStatus>): Flow<ProductEntity>
    fun findByBoothIdOrderBySortOrder(boothId: Long): Flow<ProductEntity>
}