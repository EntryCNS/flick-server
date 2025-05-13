package com.flick.place.domain.auth.service

import com.flick.common.error.CustomException
import com.flick.domain.booth.entity.BoothEntity
import com.flick.domain.booth.enums.BoothStatus
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.place.domain.auth.dto.request.LoginRequest
import com.flick.place.domain.auth.dto.request.RefreshRequest
import com.flick.place.domain.auth.dto.request.RegisterRequest
import com.flick.place.infra.security.JwtError
import com.flick.place.infra.security.JwtPayload
import com.flick.place.infra.security.JwtProvider
import com.flick.place.infra.security.JwtType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class AuthService(
    private val boothRepository: BoothRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val transactionalOperator: TransactionalOperator
) {
    suspend fun login(request: LoginRequest): JwtPayload = transactionalOperator.executeAndAwait {
        val booth = boothRepository.findByUsername(request.username)
            ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)

        if (!passwordEncoder.matches(request.password, booth.passwordHash)) {
            throw CustomException(BoothError.BOOTH_PASSWORD_NOT_MATCH)
        }

        when (booth.status) {
            BoothStatus.PENDING -> throw CustomException(BoothError.BOOTH_NOT_APPROVED)
            BoothStatus.REJECTED -> throw CustomException(BoothError.BOOTH_REJECTED)
            BoothStatus.INACTIVE -> throw CustomException(BoothError.BOOTH_INACTIVE)
            else -> jwtProvider.generateBoothToken(booth.id!!)
        }
    }

    suspend fun register(request: RegisterRequest) = transactionalOperator.executeAndAwait {
        if (boothRepository.existsByUsername(request.username)) {
            throw CustomException(BoothError.BOOTH_USERNAME_ALREADY_EXISTS)
        }

        boothRepository.save(
            BoothEntity(
                username = request.username,
                passwordHash = passwordEncoder.encode(request.password),
                name = request.name,
                description = request.description
            )
        )
    }

    fun refresh(request: RefreshRequest): JwtPayload {
        if (jwtProvider.getType(request.refreshToken) != JwtType.REFRESH) {
            throw CustomException(JwtError.INVALID_TOKEN_TYPE)
        }

        val boothId = jwtProvider.getBoothId(request.refreshToken)
        return jwtProvider.generateBoothToken(boothId)
    }
}