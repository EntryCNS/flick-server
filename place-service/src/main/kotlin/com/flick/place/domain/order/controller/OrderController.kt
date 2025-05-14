package com.flick.place.domain.order.controller

import com.flick.place.domain.order.dto.request.CreateOrderRequest
import com.flick.place.domain.order.service.OrderService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
class OrderController(private val orderService: OrderService) {
    @GetMapping
    suspend fun getOrders() = orderService.getOrders()

    @GetMapping("/{orderId}")
    suspend fun getOrder(@PathVariable orderId: Long) = orderService.getOrder(orderId)

    @PostMapping
    suspend fun createOrder(@RequestBody request: CreateOrderRequest) = orderService.createOrder(request)

    @PostMapping("/{orderId}/cancel")
    suspend fun cancelOrder(@PathVariable orderId: Long) {
        orderService.cancelOrder(orderId)
    }
}