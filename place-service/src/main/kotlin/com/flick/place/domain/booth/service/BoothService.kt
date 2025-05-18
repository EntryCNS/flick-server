package com.flick.place.domain.booth.service

import com.flick.common.error.CustomException
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.domain.order.repository.PaymentRepository
import com.flick.place.domain.booth.dto.response.BoothSaleResponse
import com.flick.place.domain.booth.dto.response.CheckBoothResponse
import com.flick.place.infra.security.SecurityHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service

@Service
class BoothService(
    private val boothRepository: BoothRepository,
    private val paymentRepository: PaymentRepository,
    private val securityHolder: SecurityHolder,
) {
    suspend fun checkBooth(username: String) = CheckBoothResponse(
        exists = boothRepository.existsByUsername(username)
    )

    suspend fun getSales(): Flow<BoothSaleResponse> {
        val booth = boothRepository.findById(securityHolder.getBoothId())
            ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)
        val payments = paymentRepository.findAllByBoothIdOrderByCreatedAtDesc(booth.id!!)

        return payments.map {
            BoothSaleResponse(
                amount = it.amount,
                timestamp = it.createdAt
            )
        }
    }
}