package com.flick.admin.domain.booth.service

import com.flick.admin.domain.booth.dto.response.BoothDetailResponse
import com.flick.admin.domain.booth.dto.response.BoothRankingResponse
import com.flick.admin.domain.booth.dto.response.BoothResponse
import com.flick.admin.infra.websocket.BoothWebSocketHandler
import com.flick.common.error.CustomException
import com.flick.domain.booth.enums.BoothStatus
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.domain.product.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.LocalDateTime

@Service
class BoothService(
    private val boothRepository: BoothRepository,
    private val boothWebSocketHandler: BoothWebSocketHandler,
    private val transactionalOperator: TransactionalOperator,
    private val productRepository: ProductRepository,
) {
    suspend fun getBooths(statuses: List<BoothStatus>): Flow<BoothResponse> {
        val booths = if (statuses.isEmpty()) boothRepository.findAll() else boothRepository.findAllByStatusIn(statuses)

        return booths.map {
            BoothResponse(
                id = it.id!!,
                name = it.name,
                description = it.description,
                imageUrl = it.imageUrl,
                status = it.status,
                totalSales = it.totalSales,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
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
            imageUrl = booth.imageUrl,
            totalSales = booth.totalSales,
            products = products.map {
                BoothDetailResponse.Product(
                    id = it.id!!,
                    name = it.name,
                    description = it.description,
                    price = it.price,
                    imageUrl = it.imageUrl,
                    stock = it.stock,
                    status = it.status,
                    sortOrder = it.sortOrder
                )
            }.toList()
        )
    }

    suspend fun approveBooth(boothId: Long) = transactionalOperator.executeAndAwait {
        boothRepository.save(getBoothEntity(boothId).copy(status = BoothStatus.APPROVED))
    }

    suspend fun rejectBooth(boothId: Long) = transactionalOperator.executeAndAwait {
        boothRepository.save(getBoothEntity(boothId).copy(status = BoothStatus.REJECTED))
    }

    suspend fun publishRanking(boothId: Long) {
        val booth = getBoothEntity(boothId)

        boothWebSocketHandler.sendRankingUpdate(
            BoothRankingResponse(
                id = booth.id!!,
                totalSales = booth.totalSales,
                name = booth.name,
                timestamp = LocalDateTime.now(),
            )
        )
    }

    suspend fun getBoothRankings() = getCurrentRankings()

    private suspend fun getBoothEntity(boothId: Long) =
        boothRepository.findById(boothId) ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)

    private suspend fun getCurrentRankings() = boothRepository.findAllByStatusOrderByTotalSalesDesc(BoothStatus.APPROVED).toList()
        .map {
            BoothRankingResponse(
                id = it.id!!,
                totalSales = it.totalSales,
                name = it.name,
                timestamp = LocalDateTime.now(),
            )
        }
}