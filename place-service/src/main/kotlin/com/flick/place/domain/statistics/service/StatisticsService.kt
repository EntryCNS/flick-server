package com.flick.place.domain.statistics.service

import com.flick.common.error.CustomException
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.domain.order.enums.OrderStatus
import com.flick.domain.order.repository.OrderRepository
import com.flick.place.domain.statistics.dto.response.StatisticsResponse
import com.flick.place.infra.security.SecurityHolder
import org.springframework.stereotype.Service

@Service
class StatisticsService(
    private val boothRepository: BoothRepository,
    private val securityHolder: SecurityHolder,
    private val orderRepository: OrderRepository
) {
    suspend fun getStatistics(): StatisticsResponse {
        val booth = boothRepository.findById(securityHolder.getBoothId())
            ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)
        val totalOrders = orderRepository.countByBoothIdAndStatus(booth.id!!, OrderStatus.PAID)

        return StatisticsResponse(
            totalSales = booth.totalSales,
            totalOrders = totalOrders,
        )
    }
}