package com.flick.core.domain.order.controller

import com.flick.core.domain.order.service.OrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(private val orderService: OrderService) {
    @GetMapping("/{orderId}")
    suspend fun getOrder(@PathVariable orderId: Long) = orderService.getOrder(orderId)
}