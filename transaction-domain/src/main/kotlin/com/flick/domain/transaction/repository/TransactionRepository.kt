package com.flick.domain.transaction.repository

import com.flick.domain.transaction.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TransactionRepository: CoroutineCrudRepository<TransactionEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): Flow<TransactionEntity>
}