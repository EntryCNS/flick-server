package com.flick.core.domain.notification.controller

import com.flick.core.domain.notification.service.NotificationService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/notifications")
class NotificationController(private val notificationService: NotificationService) {
    @GetMapping("/my")
    suspend fun getMyNotifications() = notificationService.getMyNotifications()

    @PostMapping("/{notificationId}/read")
    suspend fun readNotification(@PathVariable notificationId: Long) =
        notificationService.readNotification(notificationId)
}