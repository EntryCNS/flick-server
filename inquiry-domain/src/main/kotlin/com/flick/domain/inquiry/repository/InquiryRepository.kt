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
        LIMIT :size OFFSET :offset
        """
    )
    fun findAll(@Param("size") size: Int, @Param("offset") offset: Int): Flow<InquiryEntity>

    @Query(
        """
        SELECT * FROM inquiries
        WHERE category = :category
        ORDER BY id DESC
        LIMIT :size OFFSET :offset
        """
    )
    fun findAllByCategory(
        @Param("category") category: InquiryCategory,
        @Param("size") size: Int,
        @Param("offset") offset: Int
    ): Flow<InquiryEntity>

    suspend fun countByCategory(category: InquiryCategory): Long

    @Query("SELECT category, COUNT(*) as count FROM inquiries GROUP BY category ORDER BY count DESC")
    fun countGroupByCategory(): Flow<CategoryCount>
}

data class CategoryCount(val category: String, val count: Long)