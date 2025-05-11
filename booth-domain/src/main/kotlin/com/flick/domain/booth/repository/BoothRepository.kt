package com.flick.domain.booth.repository

import com.flick.domain.booth.entity.BoothEntity
import com.flick.domain.booth.enums.BoothStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface BoothRepository : CoroutineCrudRepository<BoothEntity, Long> {
    suspend fun findByUsername(username: String): BoothEntity?
    suspend fun findByName(name: String): BoothEntity?
    suspend fun existsByUsername(username: String): Boolean
    fun findAllByStatusIn(status: List<BoothStatus>): Flow<BoothEntity>
    fun findAllByOrderByTotalSalesDesc(): Flow<BoothEntity>
}