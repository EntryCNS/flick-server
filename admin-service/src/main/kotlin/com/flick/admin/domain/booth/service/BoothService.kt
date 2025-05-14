package com.flick.admin.domain.booth.service

import com.flick.admin.domain.booth.BoothRankingCache
import com.flick.admin.domain.booth.dto.response.BoothRankingResponse
import com.flick.admin.domain.booth.dto.response.BoothResponse
import com.flick.admin.infra.websocket.BoothWebSocketHandler
import com.flick.common.error.CustomException
import com.flick.domain.booth.enums.BoothStatus
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class BoothService(
    private val boothRepository: BoothRepository,
    private val boothWebSocketHandler: BoothWebSocketHandler,
    private val transactionalOperator: TransactionalOperator,
    private val rankingCache: BoothRankingCache,
) {
    suspend fun getBooths(statuses: List<BoothStatus>): Flow<BoothResponse> {
        val booths = if (statuses.isEmpty()) boothRepository.findAll() else boothRepository.findAllByStatusIn(statuses)

        return booths.map {
            BoothResponse(
                id = it.id!!,
                name = it.name,
                description = it.description,
                status = it.status,
                totalSales = it.totalSales,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
    }

    suspend fun approveBooth(boothId: Long) = transactionalOperator.executeAndAwait {
        boothRepository.save(getBooth(boothId).copy(status = BoothStatus.APPROVED))
    }

    suspend fun rejectBooth(boothId: Long) = transactionalOperator.executeAndAwait {
        boothRepository.save(getBooth(boothId).copy(status = BoothStatus.REJECTED))
    }

    suspend fun publishRankings() {
        val current = getCurrentRankings()

        val previousMap = rankingCache.previous.associateBy { it.id }
        val changed = current.filter { currentItem ->
            val previousItem = previousMap[currentItem.id]
            previousItem == null || previousItem.rank != currentItem.rank || previousItem.totalSales != currentItem.totalSales
        }

        if (changed.isNotEmpty()) {
            boothWebSocketHandler.sendRankingUpdate(changed)
            rankingCache.previous = current
        }
    }

    suspend fun getBoothRankings() = getCurrentRankings()

    private suspend fun getBooth(boothId: Long) =
        boothRepository.findById(boothId) ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)

    private suspend fun getCurrentRankings() = boothRepository.findAllByOrderByTotalSalesDesc().toList()
        .mapIndexed { index, booth ->
            BoothRankingResponse(
                rank = index + 1,
                id = booth.id!!,
                name = booth.name,
                totalSales = booth.totalSales,
            )
        }
}