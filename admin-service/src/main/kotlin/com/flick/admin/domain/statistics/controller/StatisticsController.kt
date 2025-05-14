package com.flick.admin.domain.statistics.controller

import com.flick.admin.domain.statistics.dto.StatisticsResponse
import com.flick.admin.domain.statistics.service.StatisticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/statistics")
class StatisticsController(
    private val statisticsService: StatisticsService
) {
    @GetMapping
    suspend fun getStatistics(): StatisticsResponse = statisticsService.getStatistics()
}