package com.flick.core.domain.booth.service

import com.flick.common.error.CustomException
import com.flick.core.domain.booth.dto.response.BoothDetailResponse
import com.flick.core.domain.booth.dto.response.BoothResponse
import com.flick.domain.booth.enums.BoothStatus
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.domain.product.enums.ProductStatus
import com.flick.domain.product.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class BoothService(private val boothRepository: BoothRepository, private val productRepository: ProductRepository) {
    suspend fun getBooths(): Flow<BoothResponse> {
        val booths = boothRepository.findAllByStatus(BoothStatus.APPROVED)

        return booths.map {
            BoothResponse(
                id = it.id!!,
                name = it.name,
                description = it.description,
                imageUrl = it.imageUrl
            )
        }
    }

    suspend fun getBooth(boothId: Long): BoothDetailResponse {
        val booth = boothRepository.findById(boothId)
            ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)

        if (booth.status != BoothStatus.APPROVED)
            throw CustomException(BoothError.BOOTH_NOT_APPROVED)

        val products = productRepository.findAllByBoothId(booth.id!!)

        return BoothDetailResponse(
            id = booth.id!!,
            name = booth.name,
            description = booth.description,
            imageUrl = booth.imageUrl,
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