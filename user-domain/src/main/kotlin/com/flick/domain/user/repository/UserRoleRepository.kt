package com.flick.domain.user.repository

import com.flick.domain.user.entity.UserRoleEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRoleRepository : CoroutineCrudRepository<UserRoleEntity, Long> {
    fun findAllByUserId(userId: Long): Flow<UserRoleEntity>
    fun findAllByUserIdIn(userIds: List<Long>): Flow<UserRoleEntity>
}