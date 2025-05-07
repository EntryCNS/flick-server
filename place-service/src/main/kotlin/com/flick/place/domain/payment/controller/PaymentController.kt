package com.flick.place.domain.payment.controller

import com.flick.place.domain.payment.dto.CreateQrPaymentRequest
import com.flick.place.domain.payment.dto.CreateStudentIdPaymentRequest
import com.flick.place.domain.payment.service.PaymentService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payments")
class PaymentController(private val paymentService: PaymentService) {
    @PostMapping("/qr")
    suspend fun createQrPayment(@RequestBody request: CreateQrPaymentRequest) = paymentService.createQrPayment(request)

    @PostMapping("/student-id")
    suspend fun createStudentIdPayment(@RequestBody request: CreateStudentIdPaymentRequest) =
        paymentService.createStudentIdPayment(request)
}