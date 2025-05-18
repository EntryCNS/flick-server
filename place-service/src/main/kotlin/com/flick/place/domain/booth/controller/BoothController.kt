package com.flick.place.domain.booth.controller

import com.flick.place.domain.booth.service.BoothService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/booths")
class BoothController(private val boothService: BoothService) {
    @GetMapping("/check")
    suspend fun checkBooth(@RequestParam username: String) = boothService.checkBooth(username)

    @GetMapping("/sales")
    suspend fun getSales() = boothService.getSales()
}