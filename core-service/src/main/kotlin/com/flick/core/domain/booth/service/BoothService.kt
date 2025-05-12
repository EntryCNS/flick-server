package com.flick.core.domain.booth.service

import com.flick.common.error.CustomException
import com.flick.core.domain.booth.dto.response.BoothDetailResponse
import com.flick.core.domain.booth.dto.response.BoothResponse
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.domain.payment.enums.ProductStatus
import com.flick.domain.payment.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class BoothService(private val boothRepository: BoothRepository, private val productRepository: ProductRepository) {
    suspend fun getBooths(): Flow<BoothResponse> {
        val booths = boothRepository.findAll()

        return booths.map {
            BoothResponse(
                id = it.id!!,
                name = it.name,
                description = it.description
            )
        }
    }

    suspend fun getBooth(boothId: Long): BoothDetailResponse {
        val booth = boothRepository.findById(boothId)
            ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)
        val products = productRepository.findAllByBoothId(booth.id!!)

        return BoothDetailResponse(
            id = booth.id!!,
            name = booth.name,
            description = booth.description,
            imageUrl = null,
            products = products.map {
                BoothDetailResponse.Product(
                    id = it.id!!,
                    name = it.name,
                    description = it.description,
                    price = it.price,
                    imageUrl = it.imageUrl,
                    isSoldOut = it.status == ProductStatus.SOLD_OUT
                )
            }.toList()
        )
    }
}