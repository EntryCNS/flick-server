package com.flick.domain.booth.repository

import com.flick.domain.booth.entity.BoothEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface BoothRepository : CoroutineCrudRepository<BoothEntity, Long> {
    suspend fun findByUsername(username: String): BoothEntity?
    suspend fun findByName(name: String): BoothEntity?
}