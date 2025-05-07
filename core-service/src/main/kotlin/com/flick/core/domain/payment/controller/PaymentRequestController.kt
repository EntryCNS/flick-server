package com.flick.core.domain.payment.controller

import com.flick.core.domain.payment.dto.ConfirmPaymentRequestRequest
import com.flick.core.domain.payment.service.PaymentRequestService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payments/requests")
class PaymentRequestController(private val paymentRequestService: PaymentRequestService) {
    @GetMapping
    suspend fun getPaymentRequest(@RequestParam token: String) = paymentRequestService.getPaymentRequest(token)

    @PostMapping("/confirm")
    suspend fun confirmPaymentRequest(@RequestBody request: ConfirmPaymentRequestRequest) = paymentRequestService.confirmPaymentRequest(request)
}