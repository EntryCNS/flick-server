package com.flick.admin.domain.inquiry.controller

import com.flick.admin.domain.inquiry.service.InquiryService
import com.flick.domain.inquiry.enums.InquiryCategory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/inquiries")
class InquiryController(private val inquiryService: InquiryService) {
    @GetMapping
    suspend fun getInquiries(
        @RequestParam(required = false) category: InquiryCategory?,
        @RequestParam(required = false, defaultValue = "1") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ) = inquiryService.getInquiries(category, page, size)

    @GetMapping("/{inquiryId}")
    suspend fun getInquiry(@PathVariable inquiryId: Long) = inquiryService.getInquiry(inquiryId)
}