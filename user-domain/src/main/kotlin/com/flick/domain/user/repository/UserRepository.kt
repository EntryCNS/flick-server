package com.flick.domain.user.repository

import com.flick.domain.user.entity.UserEntity
import com.flick.domain.user.enums.UserRoleType
import com.flick.domain.user.query.UserWithRoles
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

    @Query(
        """
        WITH RankedUsers AS (
            SELECT 
                u.*,
                array_agg(DISTINCT r.role) as roles,
                COUNT(*) OVER() as total_count,
                ROW_NUMBER() OVER (ORDER BY u.name ASC) as row_num
            FROM users u
            LEFT JOIN user_roles r ON r.user_id = u.id
            WHERE (:name IS NULL OR u.name ILIKE concat('%', :name, '%'))
              AND (:grade IS NULL OR u.grade = :grade)
              AND (:room IS NULL OR u.room = :room)
              AND (:role IS NULL OR r.role = :role)
            GROUP BY u.id
        )
        SELECT *
        FROM RankedUsers
        WHERE row_num > :offset
        AND row_num <= (:offset + :limit)
    """
    )
    fun findAllByFilters(
        @Param("name") name: String?,
        @Param("grade") grade: Int?,
        @Param("room") room: Int?,
        @Param("role") role: UserRoleType?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flow<UserWithRoles>

    @Query("SELECT * FROM users WHERE id = :id FOR UPDATE SKIP LOCKED")
    suspend fun findOneByIdForUpdate(@Param("id") id: Long): UserEntity?
}