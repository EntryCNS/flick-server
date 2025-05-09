package com.flick.admin.domain.booth.service

import com.flick.admin.domain.booth.dto.response.BoothResponse
import com.flick.common.error.CustomException
import com.flick.domain.booth.enums.BoothStatus
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BoothService(private val boothRepository: BoothRepository) {
    @Transactional(readOnly = true)
    suspend fun getBooths(rawStatuses: List<String>?): Flow<BoothResponse> {
        val booths = when (rawStatuses) {
            null, emptyList<BoothStatus>() -> boothRepository.findAll()
            else -> {
                val statuses = rawStatuses.map { BoothStatus.fromCode(it) }
                boothRepository.findAllByStatusIn(statuses)
            }
        }

        return booths.map {
            BoothResponse(
                id = it.id!!,
                name = it.name,
                description = it.description!!,
                status = it.status,
                totalSales = it.totalSales,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
    }

    @Transactional
    suspend fun approveBooth(boothId: Long) {
        val booth = getBooth(boothId)

        boothRepository.save(booth.copy(status = BoothStatus.APPROVED))
    }

    @Transactional
    suspend fun rejectBooth(boothId: Long) {
        val booth = getBooth(boothId)

        boothRepository.save(booth.copy(status = BoothStatus.REJECTED))
    }

    private suspend fun getBooth(boothId: Long) =
        boothRepository.findById(boothId) ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)
}