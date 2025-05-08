package com.flick.admin.domain.booth.controller

import com.flick.admin.domain.booth.service.BoothService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("/booths")
class BoothController(private val boothService: BoothService) {
    @GetMapping
    suspend fun getBooths() = boothService.getBooths()
}