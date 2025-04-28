package com.flick.domain.transaction.repository

import com.flick.domain.transaction.entity.Transaction
import com.flick.domain.transaction.enums.TransactionType
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDateTime

interface TransactionRepository : CoroutineCrudRepository<Transaction, Long> {
    fun findByUserId(userId: Long): Flow<Transaction>
    fun findByUserIdAndType(userId: Long, type: TransactionType): Flow<Transaction>
    fun findByOrderId(orderId: Long): Flow<Transaction>

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND created_at >= :startDate AND created_at <= :endDate")
    fun findByUserIdAndDateRange(userId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Flow<Transaction>
}