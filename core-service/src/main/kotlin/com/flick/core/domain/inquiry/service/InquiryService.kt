package com.flick.core.domain.inquiry.service

import com.flick.core.domain.inquiry.dto.request.CreateInquiryRequest
import com.flick.core.infra.security.SecurityHolder
import com.flick.domain.inquiry.entity.InquiryEntity
import com.flick.domain.inquiry.repository.InquiryRepository
import org.springframework.stereotype.Service

@Service
class InquiryService(private val securityHolder: SecurityHolder, private val inquiryRepository: InquiryRepository) {
    suspend fun createInquiry(request: CreateInquiryRequest) {
        val userId = securityHolder.getUserId()

        inquiryRepository.save(InquiryEntity(
            userId = userId,
            title = request.title,
            content = request.content,
            category = request.category
        ))
    }
}