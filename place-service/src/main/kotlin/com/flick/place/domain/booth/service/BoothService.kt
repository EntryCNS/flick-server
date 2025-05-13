package com.flick.place.domain.booth.service

import com.flick.domain.booth.repository.BoothRepository
import com.flick.place.domain.booth.dto.response.CheckBoothResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class BoothService(
    private val boothRepository: BoothRepository,
    private val transactionalOperator: TransactionalOperator
) {
    suspend fun checkBooth(username: String) = transactionalOperator.executeAndAwait {
        CheckBoothResponse(
            exists = boothRepository.existsByUsername(username)
        )
    }
}