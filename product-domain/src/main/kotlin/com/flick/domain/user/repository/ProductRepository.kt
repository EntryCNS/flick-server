package com.flick.domain.payment.repository

import com.flick.domain.payment.entity.ProductEntity
import com.flick.domain.payment.enums.ProductStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProductRepository: CoroutineCrudRepository<ProductEntity, Long> {
    fun findAllByBoothId(boothId: Long): Flow<ProductEntity>
    fun findAllByBoothIdAndStatus(boothId: Long, status: ProductStatus): Flow<ProductEntity>
}