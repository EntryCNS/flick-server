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
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.LocalDateTime

@Service
class AuthService(
    private val dAuthClient: DAuthClient,
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val jwtProvider: JwtProvider,
    private val transactionalOperator: TransactionalOperator
) {
    suspend fun login(request: LoginRequest): JwtPayload {
        val token = dAuthClient.login(request.id, request.password)
        val dAuthUser = dAuthClient.getUser(token.accessToken)

        val user = transactionalOperator.executeAndAwait {
            val existingUser = userRepository.findByDAuthId(dAuthUser.uniqueId)

            if (existingUser == null) {
                val newUser = userRepository.save(
                    UserEntity(
                        dAuthId = dAuthUser.uniqueId,
                        name = dAuthUser.name,
                        email = dAuthUser.email,
                        grade = dAuthUser.grade,
                        room = dAuthUser.room,
                        number = dAuthUser.number,
                        profileUrl = dAuthUser.profileImage,
                        lastLoginAt = LocalDateTime.now()
                    )
                )

                userRoleRepository.save(
                    UserRoleEntity(
                        userId = newUser.id!!,
                        role = if (dAuthUser.role == "TEACHER") UserRoleType.TEACHER else UserRoleType.STUDENT
                    )
                )

                newUser
            } else {
                userRepository.save(
                    existingUser.copy(
                        name = dAuthUser.name,
                        email = dAuthUser.email,
                        grade = dAuthUser.grade,
                        room = dAuthUser.room,
                        number = dAuthUser.number,
                        profileUrl = dAuthUser.profileImage,
                        lastLoginAt = LocalDateTime.now()
                    )
                )
            }
        }

        return jwtProvider.generateToken(user.id!!)
    }

    fun refresh(request: RefreshRequest): JwtPayload {
        val userId = jwtProvider.getUserId(request.refreshToken)
        return jwtProvider.generateToken(userId)
    }
}