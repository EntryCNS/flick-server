package com.flick.place.domain.statistics.controller

import com.flick.place.domain.statistics.service.StatisticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/statistics")
class StatisticsController(private val statisticsService: StatisticsService) {
    @GetMapping
    suspend fun getStatistics() = statisticsService.getStatistics()
}