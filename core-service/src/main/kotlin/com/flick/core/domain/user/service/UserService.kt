package com.flick.core.domain.user.service

import com.flick.common.error.CustomException
import com.flick.core.domain.user.dto.request.PushTokenRequest
import com.flick.core.domain.user.dto.response.UserResponse
import com.flick.core.infra.security.SecurityHolder
import com.flick.domain.user.enums.UserRoleType
import com.flick.domain.user.error.UserError
import com.flick.domain.user.repository.UserRepository
import com.flick.domain.user.repository.UserRoleRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class UserService(
    private val securityHolder: SecurityHolder,
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
) {
    suspend fun getMyInfo(): UserResponse {
        val userId = securityHolder.getUserId()
        val user = userRepository.findById(userId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)
        val roles = userRoleRepository.findAllByUserId(userId).toList()
        val isTeacher = roles.any { it.role == UserRoleType.TEACHER }

        return UserResponse(
            id = user.id!!,
            dAuthId = user.dAuthId,
            name = user.name,
            email = user.email,
            role = if (isTeacher) UserRoleType.TEACHER else UserRoleType.STUDENT,
            grade = user.grade,
            room = user.room,
            number = user.number,
            profileUrl = user.profileUrl,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
        )
    }

    suspend fun getMyBalance(): Long {
        val userId = securityHolder.getUserId()
        val user = userRepository.findById(userId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        return user.balance
    }

    suspend fun registerPushToken(request: PushTokenRequest) {
        val userId = securityHolder.getUserId()
        val user = userRepository.findById(userId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        userRepository.findAllByPushToken(request.token)
            .collect {
                if (it.id != userId)
                    userRepository.save(it.copy(pushToken = null))
            }

        userRepository.save(user.copy(pushToken = request.token))
    }

    suspend fun unregisterPushToken() {
        val userId = securityHolder.getUserId()
        val user = userRepository.findById(userId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        userRepository.save(user.copy(pushToken = null))
    }
}