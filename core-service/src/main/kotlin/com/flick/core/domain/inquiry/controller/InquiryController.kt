package com.flick.core.domain.inquiry.controller

import com.flick.core.domain.inquiry.dto.request.CreateInquiryRequest
import com.flick.core.domain.inquiry.service.InquiryService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/inquiries")
class InquiryController(private val inquiryService: InquiryService) {
    @PostMapping
    suspend fun createInquiry(@RequestBody request: CreateInquiryRequest) = inquiryService.createInquiry(request)
}