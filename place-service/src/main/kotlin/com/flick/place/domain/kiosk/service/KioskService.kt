package com.flick.place.domain.kiosk.service

import com.flick.common.error.CustomException
import com.flick.domain.booth.error.BoothError
import com.flick.place.domain.kiosk.dto.request.RegisterKioskRequest
import com.flick.place.domain.kiosk.dto.response.GenerateKioskRegistrationTokenResponse
import com.flick.place.infra.security.JwtPayload
import com.flick.place.infra.security.JwtProvider
import com.flick.place.infra.security.SecurityHolder
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.Duration

@Service
class KioskService(
    private val securityHolder: SecurityHolder,
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val jwtProvider: JwtProvider,
    private val transactionalOperator: TransactionalOperator
) {
    companion object {
        private const val KIOSK_TOKEN_PREFIX = "kiosk:registration:token"
        private val KIOSK_TOKEN_EXPIRATION = Duration.ofMinutes(5)
    }

    suspend fun generateKioskRegistrationToken(): GenerateKioskRegistrationTokenResponse = transactionalOperator.executeAndAwait {
        val boothId = securityHolder.getBoothId()
        val token = (1..20).map { (('a'..'z') + ('0'..'9')).random() }.joinToString("")

        val key = "$KIOSK_TOKEN_PREFIX:$token"
        redisTemplate.opsForValue().set(key, boothId.toString())
            .then(redisTemplate.expire(key, KIOSK_TOKEN_EXPIRATION))
            .awaitSingle()

        GenerateKioskRegistrationTokenResponse(registrationToken = token)
    }

    suspend fun register(request: RegisterKioskRequest): JwtPayload = transactionalOperator.executeAndAwait {
        val key = "$KIOSK_TOKEN_PREFIX:${request.registrationToken}"
        val boothId = redisTemplate.opsForValue().get(key).awaitSingleOrNull()
            ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)

        redisTemplate.delete(key).awaitSingle()

        jwtProvider.generateKioskToken(boothId.toLong())
    }
}