package com.flick.place.domain.order.controller

import com.flick.domain.order.enums.OrderStatus
import com.flick.place.domain.order.dto.CreateOrderRequest
import com.flick.place.domain.order.dto.OrderResponse
import com.flick.place.domain.order.dto.PaymentRequestResponse
import com.flick.place.domain.order.service.OrderService
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(private val orderService: OrderService) {
    @GetMapping
    suspend fun getOrders(@RequestParam status: OrderStatus? = null): Flow<OrderResponse> =
        orderService.getOrders(status)

    @GetMapping("/today")
    suspend fun getTodayOrders(): Flow<OrderResponse> =
        orderService.getTodayOrders()

    @GetMapping("/{orderId}")
    suspend fun getOrder(@PathVariable orderId: Long): OrderResponse =
        orderService.getOrder(orderId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createOrder(@RequestBody request: CreateOrderRequest): OrderResponse =
        orderService.createOrder(request)

    @PostMapping("/{orderId}/payment-requests/qr")
    suspend fun createQrPaymentRequest(@PathVariable orderId: Long): PaymentRequestResponse =
        orderService.createQrPaymentRequest(orderId)

    @PostMapping("/{orderId}/payment-requests/student-id")
    suspend fun createStudentIdPaymentRequest(
        @PathVariable orderId: Long,
        @RequestBody studentId: String
    ): PaymentRequestResponse =
        orderService.createStudentIdPaymentRequest(orderId, studentId)

    @GetMapping("/{orderId}/payment-request")
    suspend fun getPaymentRequest(@PathVariable orderId: Long): PaymentRequestResponse? =
        orderService.getPaymentRequest(orderId)
}