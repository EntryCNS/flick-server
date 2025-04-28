package com.flick.domain.user.repository

import com.flick.domain.user.entity.User
import com.flick.domain.user.enums.UserRole
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<User, Long> {
    suspend fun findByUniqueId(uniqueId: String): User?
    suspend fun findByLoginId(loginId: String): User?
    fun findByRole(role: UserRole): Flow<User>

    @Query("SELECT * FROM users WHERE grade = :grade AND room = :room")
    fun findByGradeAndRoom(grade: Int, room: Int): Flow<User>

    @Query("SELECT * FROM users WHERE grade = :grade AND room = :room AND number = :number")
    suspend fun findByGradeAndRoomAndNumber(grade: Int, room: Int, number: Int): User?
}