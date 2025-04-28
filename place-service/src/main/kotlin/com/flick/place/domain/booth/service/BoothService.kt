package com.flick.place.domain.booth.service

import com.flick.domain.booth.enums.BoothStatus
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.common.error.CustomException
import com.flick.domain.booth.entity.Booth
import com.flick.place.domain.booth.dto.AuthResponse
import com.flick.place.domain.booth.dto.LoginBoothRequest
import com.flick.place.domain.booth.dto.BoothResponse
import com.flick.place.domain.booth.dto.RegisterBoothRequest
import com.flick.place.infra.security.JwtHolder
import com.flick.place.infra.security.JwtProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class BoothService(
    private val boothRepository: BoothRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val jwtHolder: JwtHolder
) {
    suspend fun login(request: LoginBoothRequest): AuthResponse {
        val booth = boothRepository.findByLoginId(request.loginId)
            ?: throw CustomException(BoothError.INVALID_CREDENTIALS)

        if (!passwordEncoder.matches(request.password, booth.passwordHash)) {
            throw CustomException(BoothError.INVALID_CREDENTIALS)
        }

        when (booth.status) {
            BoothStatus.PENDING -> throw CustomException(BoothError.BOOTH_PENDING)
            BoothStatus.REJECTED -> throw CustomException(BoothError.BOOTH_REJECTED)
            BoothStatus.INACTIVE -> throw CustomException(BoothError.BOOTH_INACTIVE)
            else -> {}
        }

        val accessToken = jwtProvider.generateBoothToken(booth.id!!)

        return AuthResponse(
            accessToken = accessToken,
        )
    }

    suspend fun register(request: RegisterBoothRequest) {
        if (boothRepository.findByLoginId(request.loginId) != null)
            throw CustomException(BoothError.BOOTH_LOGIN_ID_DUPLICATED)

        boothRepository.save(Booth(
            loginId = request.loginId,
            passwordHash = passwordEncoder.encode(request.password),
            name = request.name,
            description = request.description,
            location = request.location,
        ))
    }

    suspend fun getMyBooth(): BoothResponse {
        val booth = boothRepository.findById(jwtHolder.getBoothId())
            ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)

        return BoothResponse.of(booth)
    }
}