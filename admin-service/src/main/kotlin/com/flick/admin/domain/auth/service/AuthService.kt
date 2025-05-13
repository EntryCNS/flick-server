package com.flick.admin.domain.auth.service

import com.flick.admin.domain.auth.dto.request.LoginRequest
import com.flick.admin.domain.auth.dto.request.RefreshRequest
import com.flick.admin.infra.dauth.client.DAuthClient
import com.flick.admin.infra.security.JwtPayload
import com.flick.admin.infra.security.JwtProvider
import com.flick.common.error.CustomException
import com.flick.domain.user.enums.UserRoleType
import com.flick.domain.user.error.UserError
import com.flick.domain.user.repository.UserRepository
import com.flick.domain.user.repository.UserRoleRepository
import kotlinx.coroutines.flow.firstOrNull
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

        return transactionalOperator.executeAndAwait {
            val user = userRepository.findByDAuthId(dAuthUser.uniqueId)
                ?: throw CustomException(UserError.USER_NOT_FOUND)

            val isAdmin = userRoleRepository.findAllByUserId(user.id!!)
                .firstOrNull { it.role == UserRoleType.ADMIN } != null

            if (!isAdmin) throw CustomException(UserError.PERMISSION_DENIED)

            val updatedUser = userRepository.save(user.copy(lastLoginAt = LocalDateTime.now()))

            jwtProvider.generateToken(updatedUser.id!!)
        }
    }

    fun refresh(request: RefreshRequest): JwtPayload {
        val userId = jwtProvider.getUserId(request.refreshToken)
        return jwtProvider.generateToken(userId)
    }
}