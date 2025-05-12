package com.flick.domain.user.repository

import com.flick.domain.user.entity.UserEntity
import com.flick.domain.user.enums.UserRoleType
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param

interface UserRepository : CoroutineCrudRepository<UserEntity, Long> {
    suspend fun findByDAuthId(dAuthId: String): UserEntity?
    suspend fun findByGradeAndRoomAndNumber(
        grade: Int,
        room: Int,
        number: Int,
    ): UserEntity?
    @Query("""
        SELECT u.* FROM users u
        JOIN user_roles r ON r.user_id = u.id
        WHERE (:name IS NULL OR u.name ILIKE '%' || :name || '%')
          AND (:grade IS NULL OR u.grade = :grade)
          AND (:room IS NULL OR u.room = :room)
          AND (:role IS NULL OR r.role = :role)
        GROUP BY u.id
        ORDER BY u.name ASC
        LIMIT :limit OFFSET :offset
    """)
    fun findFiltered(
        @Param("name") name: String?,
        @Param("grade") grade: Int?,
        @Param("room") room: Int?,
        @Param("role") role: UserRoleType?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flow<UserEntity>
    @Query("""
        SELECT COUNT(DISTINCT u.id) FROM users u
        JOIN user_roles r ON r.user_id = u.id
        WHERE (:name IS NULL OR u.name ILIKE '%' || :name || '%')
          AND (:grade IS NULL OR u.grade = :grade)
          AND (:room IS NULL OR u.room = :room)
          AND (:role IS NULL OR r.role = :role)
    """)
    suspend fun countFiltered(
        @Param("name") name: String?,
        @Param("grade") grade: Int?,
        @Param("room") room: Int?,
        @Param("role") role: UserRoleType?
    ): Long
    @Query("SELECT * FROM users WHERE id = :id FOR UPDATE")
    suspend fun findByIdForUpdate(@Param("id") id: Long): UserEntity?
}