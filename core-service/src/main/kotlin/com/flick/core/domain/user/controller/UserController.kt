package com.flick.core.domain.user.controller

import com.flick.core.domain.user.dto.request.PushTokenRequest
import com.flick.core.domain.user.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {
    @GetMapping("/me")
    suspend fun getMyInfo() = userService.getMyInfo()

    @GetMapping("/me/balance")
    suspend fun getMyBalance() = userService.getMyBalance()

    @PostMapping("/me/push-token")
    suspend fun registerPushToken(@RequestBody request: PushTokenRequest) =
        userService.registerPushToken(request)

    @DeleteMapping("/me/push-token")
    suspend fun unregisterPushToken() =
        userService.unregisterPushToken()
}