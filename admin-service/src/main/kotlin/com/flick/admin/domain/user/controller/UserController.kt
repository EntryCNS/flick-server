package com.flick.admin.domain.user.controller

import com.flick.admin.domain.user.dto.request.ChargeUserPointRequest
import com.flick.admin.domain.user.service.UserService
import com.flick.domain.user.enums.UserRoleType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {
    @GetMapping
    suspend fun getUsers(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) grade: Int?,
        @RequestParam(required = false) room: Int?,
        @RequestParam(required = false) role: UserRoleType?,
        @RequestParam page: Int = 1,
        @RequestParam size: Int = 20,
    ) = userService.getUsers(name, grade, room, role, page, size)

    @PostMapping("/{userId}/charge")
    suspend fun chargeUserPoint(@PathVariable userId: Long, @RequestBody request: ChargeUserPointRequest) = userService.chargeUserPoint(userId, request)
}