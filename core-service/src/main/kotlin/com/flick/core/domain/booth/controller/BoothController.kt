package com.flick.core.domain.booth.controller

import com.flick.core.domain.booth.service.BoothService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/booths")
class BoothController(private val boothService: BoothService) {
    @GetMapping
    suspend fun getBooths() = boothService.getBooths()

    @GetMapping("/{boothId}")
    suspend fun getBooth(@PathVariable boothId: Long) = boothService.getBooth(boothId)
}