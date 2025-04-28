package com.flick.place.domain.booth.controller

import com.flick.place.domain.booth.dto.LoginBoothRequest
import com.flick.place.domain.booth.dto.RegisterBoothRequest
import com.flick.place.domain.booth.service.BoothService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/booths")
class BoothController(private val boothService: BoothService) {
    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginBoothRequest) = boothService.login(request)

    @PostMapping("/register")
    suspend fun register(@RequestBody request: RegisterBoothRequest) = boothService.register(request)

    @GetMapping("/me")
    suspend fun getMyBooth() = boothService.getMyBooth()
}