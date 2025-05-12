package com.flick.admin.domain.user.controller

import com.flick.admin.domain.user.dto.request.ChargeUserPointRequest
import com.flick.admin.domain.user.service.UserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {
    @PostMapping("/{userId}/charge")
    suspend fun chargeUserPoint(@RequestBody request: ChargeUserPointRequest) = userService.chargeUserPoint(request)
}