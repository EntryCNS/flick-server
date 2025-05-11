package com.flick.admin.domain.booth.service

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

@Service
class BoothService(
    private val boothRepository: BoothRepository,
    private val boothWebSocketHandler: BoothWebSocketHandler,
) {
    suspend fun getBooths(statuses: List<BoothStatus>): Flow<BoothResponse> {
        val booths = if (statuses.isEmpty()) {
            boothRepository.findAll()
        } else {
            boothRepository.findAllByStatusIn(statuses)
        }

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

    suspend fun approveBooth(boothId: Long) =
        boothRepository.save(getBooth(boothId).copy(status = BoothStatus.APPROVED))

    suspend fun rejectBooth(boothId: Long) = boothRepository.save(getBooth(boothId).copy(status = BoothStatus.REJECTED))

    suspend fun publishRanking() {
        val booths = boothRepository.findAllByOrderByTotalSalesDesc().toList()

        val ranking = booths.mapIndexed { index, it ->
            BoothRankingResponse(
                rank = index + 1,
                id = it.id!!,
                name = it.name,
                totalSales = it.totalSales,
            )
        }

        boothWebSocketHandler.sendRankingUpdate(ranking)
    }

    private suspend fun getBooth(boothId: Long) =
        boothRepository.findById(boothId) ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)
}