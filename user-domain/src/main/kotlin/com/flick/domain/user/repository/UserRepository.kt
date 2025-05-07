package com.flick.domain.user.repository

import com.flick.domain.user.entity.UserEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository: CoroutineCrudRepository<UserEntity, Long> {
    suspend fun findByDAuthId(dAuthId: String): UserEntity?
    suspend fun findByGradeAndRoomAndNumber(
        grade: Int,
        room: Int,
        number: Int
    ): UserEntity?
}