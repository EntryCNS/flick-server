package com.flick.place.domain.booth.service

import com.flick.domain.booth.repository.BoothRepository
import com.flick.place.domain.booth.dto.response.CheckBoothResponse
import org.springframework.stereotype.Service

@Service
class BoothService(private val boothRepository: BoothRepository) {
    suspend fun checkBooth(username: String): CheckBoothResponse {
        return CheckBoothResponse(
            exists = boothRepository.findByUsername(username) != null
        )
    }
}