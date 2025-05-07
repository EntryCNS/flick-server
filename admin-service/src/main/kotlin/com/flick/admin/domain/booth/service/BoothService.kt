package com.flick.admin.domain.booth.service

import com.flick.admin.domain.booth.dto.response.BoothResponse
import com.flick.domain.booth.repository.BoothRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service

@Service
class BoothService(private val boothRepository: BoothRepository) {
    suspend fun getBooths(): Flow<BoothResponse> {
        val booths = boothRepository.findAll()

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
}