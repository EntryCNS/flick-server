package com.flick.domain.booth.repository

import com.flick.domain.booth.entity.Booth
import com.flick.domain.booth.enums.BoothStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface BoothRepository : CoroutineCrudRepository<Booth, Long> {
    suspend fun findByLoginId(loginId: String): Booth?
    fun findByStatus(status: BoothStatus): Flow<Booth>
    suspend fun findByName(name: String): Booth?
}