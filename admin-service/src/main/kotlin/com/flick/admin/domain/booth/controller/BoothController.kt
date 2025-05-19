package com.flick.admin.domain.booth.controller

import com.flick.admin.domain.booth.service.BoothService
import com.flick.domain.booth.enums.BoothStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/booths")
class BoothController(
    private val boothService: BoothService,
) {
    @GetMapping
    suspend fun getBooths(@RequestParam("status") statuses: List<BoothStatus> = emptyList()) =
        boothService.getBooths(statuses)

    @GetMapping("/{boothId}")
    suspend fun getBooth(@PathVariable boothId: Long) =
        boothService.getBooth(boothId)

    @PostMapping("/{boothId}/approve")
    suspend fun approveBooth(@PathVariable boothId: Long) {
        boothService.approveBooth(boothId)
    }

    @PostMapping("/{boothId}/reject")
    suspend fun rejectBooth(@PathVariable boothId: Long) {
        boothService.rejectBooth(boothId)
    }

    @GetMapping("/rankings")
    suspend fun getBoothRankings() = boothService.getBoothRankings()
}