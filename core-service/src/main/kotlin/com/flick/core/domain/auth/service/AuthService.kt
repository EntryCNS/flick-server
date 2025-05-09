package com.flick.core.domain.auth.service

import com.flick.core.domain.auth.dto.request.LoginRequest
import com.flick.core.domain.auth.dto.request.RefreshRequest
import com.flick.core.infra.dauth.DAuthClient
import com.flick.core.infra.security.JwtPayload
import com.flick.core.infra.security.JwtProvider
import com.flick.domain.user.entity.UserEntity
import com.flick.domain.user.entity.UserRoleEntity
import com.flick.domain.user.enums.UserRoleType
import com.flick.domain.user.repository.UserRepository
import com.flick.domain.user.repository.UserRoleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val dAuthClient: DAuthClient,
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val jwtProvider: JwtProvider
) {
    @Transactional
    suspend fun login(request: LoginRequest): JwtPayload {
        val token = dAuthClient.login(request.id, request.password)
        val dAuthUser = dAuthClient.getUser(token.accessToken)

        val user = userRepository.findByDAuthId(dAuthUser.uniqueId) ?: run {
            val newUser = userRepository.save(
                UserEntity(
                    dAuthId = dAuthUser.uniqueId,
                    name = dAuthUser.name,
                    email = dAuthUser.email,
                    grade = dAuthUser.grade,
                    room = dAuthUser.room,
                    number = dAuthUser.number,
                    profileUrl = dAuthUser.profileImage
                )
            )

            val role = if (dAuthUser.role == "TEACHER") UserRoleType.TEACHER else UserRoleType.STUDENT
            userRoleRepository.save(
                UserRoleEntity(
                    userId = newUser.id!!,
                    role = role
                )
            )

            newUser
        }

        val updatedUser = userRepository.save(user.copy(lastLoginAt = LocalDateTime.now()))
        return jwtProvider.generateToken(updatedUser.id!!)
    }

    suspend fun refresh(request: RefreshRequest): JwtPayload {
        val userId = jwtProvider.getUserId(request.refreshToken)

        return jwtProvider.generateToken(userId)
    }
}