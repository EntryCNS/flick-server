package com.flick.domain.booth.repository

import com.flick.domain.booth.entity.BoothEntity
import com.flick.domain.booth.enums.BoothStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface BoothRepository : CoroutineCrudRepository<BoothEntity, Long> {
    suspend fun findByUsername(username: String): BoothEntity?

    @Query("SELECT EXISTS (SELECT 1 FROM booths WHERE username = :username)")
    suspend fun existsByUsername(username: String): Boolean
    fun findAllByStatusIn(status: List<BoothStatus>): Flow<BoothEntity>
    fun findAllByStatusAndOrderByTotalSalesDesc(status: BoothStatus): Flow<BoothEntity>

    fun findAllByStatusOrderByTotalSalesDesc(status: BoothStatus): Flow<BoothEntity>
}