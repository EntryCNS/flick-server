package com.flick.core.domain.notification.controller

import com.flick.core.domain.notification.service.NotificationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notifications")
class NotificationController(private val notificationService: NotificationService) {
    @GetMapping("/my")
    suspend fun getMyNotifications() = notificationService.getMyNotifications()
}