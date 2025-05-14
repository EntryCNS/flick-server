package com.flick.domain.transaction.repository

import com.flick.domain.transaction.entity.TransactionEntity
import com.flick.domain.transaction.enums.TransactionType
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate

interface TransactionRepository : CoroutineCrudRepository<TransactionEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): Flow<TransactionEntity>

    @Query(
        """
        SELECT t.* FROM transactions t
        WHERE (:userId IS NULL OR t.user_id = :userId)
        AND (:type IS NULL OR t.type = CAST(:type AS text))
        AND (:startDate IS NULL OR DATE(t.created_at) >= :startDate)
        AND (:endDate IS NULL OR DATE(t.created_at) <= :endDate)
        ORDER BY t.created_at DESC
        LIMIT :limit OFFSET :offset
    """
    )
    fun findByFilters(
        userId: Long?,
        type: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Int,
        offset: Int
    ): Flow<TransactionEntity>

    @Query(
        """
        SELECT COUNT(*) FROM transactions t
        WHERE (:userId IS NULL OR t.user_id = :userId)
        AND (:type IS NULL OR t.type = CAST(:type AS text))
        AND (:startDate IS NULL OR DATE(t.created_at) >= :startDate)
        AND (:endDate IS NULL OR DATE(t.created_at) <= :endDate)
    """
    )
    suspend fun countByFilters(
        userId: Long?,
        type: String?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Long


    fun findAllByOrderByCreatedAtDesc(): Flow<TransactionEntity>
}