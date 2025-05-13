package com.flick.admin.domain.inquiry.controller

import com.flick.admin.domain.inquiry.service.InquiryService
import com.flick.domain.inquiry.enums.InquiryCategory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/inquiries")
class InquiryController(private val inquiryService: InquiryService) {
    @GetMapping
    suspend fun getInquiries(
        @RequestParam(required = false) category: InquiryCategory?,
        @RequestParam page: Int = 1,
        @RequestParam size: Int = 20,
    ) = inquiryService.getInquiries(category, page, size)

    @GetMapping("/{inquiryId}")
    suspend fun getInquiry(@PathVariable inquiryId: Long) = inquiryService.getInquiry(inquiryId)
}