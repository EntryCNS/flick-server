package com.flick.domain.user.repository

import com.flick.domain.user.entity.UserRoleEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param

interface UserRoleRepository: CoroutineCrudRepository<UserRoleEntity, Long> {
    fun findAllByUserId(userId: Long): Flow<UserRoleEntity>
    @Query("SELECT * FROM user_roles WHERE user_id IN (:userIds)")
    fun findAllByUserIdIn(@Param("userIds") userIds: List<Long>): Flow<UserRoleEntity>
}