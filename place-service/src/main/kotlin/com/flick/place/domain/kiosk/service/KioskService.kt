package com.flick.place.domain.kiosk.service

import com.flick.common.error.CustomException
import com.flick.domain.booth.enums.BoothStatus
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.place.domain.kiosk.dto.request.LoginKioskRequest
import com.flick.place.domain.kiosk.dto.request.RegisterKioskRequest
import com.flick.place.domain.kiosk.dto.response.GenerateKioskRegistrationTokenResponse
import com.flick.place.infra.security.JwtPayload
import com.flick.place.infra.security.JwtProvider
import com.flick.place.infra.security.SecurityHolder
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class KioskService(
    private val securityHolder: SecurityHolder,
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val jwtProvider: JwtProvider,
    private val boothRepository: BoothRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    companion object {
        private const val KIOSK_TOKEN_PREFIX = "kiosk:registration:token"
        private val KIOSK_TOKEN_EXPIRATION = Duration.ofMinutes(5)
    }

    suspend fun login(request: LoginKioskRequest): JwtPayload {
        val booth = boothRepository.findByUsername(request.username)
            ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)

        if (!passwordEncoder.matches(request.password, booth.passwordHash))
            throw CustomException(BoothError.BOOTH_PASSWORD_NOT_MATCH)

        return when (booth.status) {
            BoothStatus.PENDING -> throw CustomException(BoothError.BOOTH_NOT_APPROVED)
            BoothStatus.REJECTED -> throw CustomException(BoothError.BOOTH_REJECTED)
            BoothStatus.INACTIVE -> throw CustomException(BoothError.BOOTH_INACTIVE)
            else -> jwtProvider.generateBoothToken(booth.id!!)
        }
    }

    suspend fun generateKioskRegistrationToken(): GenerateKioskRegistrationTokenResponse {
        val boothId = securityHolder.getBoothId()
        val token = (1..20).map { (('a'..'z') + ('0'..'9')).random() }.joinToString("")

        val key = "$KIOSK_TOKEN_PREFIX:$token"
        redisTemplate.opsForValue().set(key, boothId.toString())
            .then(redisTemplate.expire(key, KIOSK_TOKEN_EXPIRATION))
            .awaitSingle()

        return GenerateKioskRegistrationTokenResponse(registrationToken = token)
    }

    suspend fun register(request: RegisterKioskRequest): JwtPayload {
        val key = "$KIOSK_TOKEN_PREFIX:${request.registrationToken}"
        val boothId = redisTemplate.opsForValue().get(key).awaitSingleOrNull()
            ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)

        redisTemplate.delete(key).awaitSingle()

        return jwtProvider.generateKioskToken(boothId.toLong())
    }
}