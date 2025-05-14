package com.flick.domain.inquiry.repository

import com.flick.domain.inquiry.entity.InquiryEntity
import com.flick.domain.inquiry.enums.InquiryCategory
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param

interface InquiryRepository : CoroutineCrudRepository<InquiryEntity, Long> {
    @Query(
        """
        SELECT * FROM inquiries
        ORDER BY id DESC
        LIMIT :limit OFFSET :offset
    """
    )
    fun findAll(@Param("limit") limit: Int, @Param("offset") offset: Int): Flow<InquiryEntity>

    @Query(
        """
        SELECT * FROM inquiries
        WHERE (:category IS NULL OR category = :category)
        ORDER BY id
        LIMIT :limit OFFSET :offset
    """
    )
    fun findAllByCategory(
        @Param("category") category: InquiryCategory,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flow<InquiryEntity>

    suspend fun countByCategory(category: InquiryCategory): Long
}