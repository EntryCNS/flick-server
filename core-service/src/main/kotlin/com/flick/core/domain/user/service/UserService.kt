package com.flick.core.domain.user.service

import com.flick.common.error.CustomException
import com.flick.core.domain.user.dto.request.PushTokenRequest
import com.flick.core.domain.user.dto.response.UserResponse
import com.flick.core.infra.security.SecurityHolder
import com.flick.domain.user.error.UserError
import com.flick.domain.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val securityHolder: SecurityHolder,
    private val userRepository: UserRepository,
) {
    suspend fun getMyInfo(): UserResponse {
        val userId = securityHolder.getUserId()
        val user = userRepository.findById(userId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        return UserResponse(
            id = user.id!!,
            dAuthId = user.dAuthId,
            name = user.name,
            email = user.email,
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

        userRepository.save(user.copy(pushToken = request.token))
    }

    suspend fun unregisterPushToken() {
        val userId = securityHolder.getUserId()
        val user = userRepository.findById(userId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        userRepository.save(user.copy(pushToken = null))
    }
}