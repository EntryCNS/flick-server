package com.flick.admin.domain.inquiry.service

import com.flick.admin.domain.inquiry.dto.response.InquiryInfoResponse
import com.flick.admin.domain.inquiry.dto.response.InquiryResponse
import com.flick.common.dto.PageResponse
import com.flick.common.error.CustomException
import com.flick.domain.inquiry.enums.InquiryCategory
import com.flick.domain.inquiry.error.InquiryError
import com.flick.domain.inquiry.repository.InquiryRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class InquiryService(private val inquiryRepository: InquiryRepository) {
    suspend fun getInquiries(
        category: InquiryCategory?,
        page: Int,
        size: Int,
    ): PageResponse<InquiryResponse> {
        val offset = (page - 1).coerceAtLeast(0) * size

        val inquiryList = inquiryRepository.findPaged(category, size, offset).toList()

        val inquiryResponses = inquiryList.map { inquiry ->
            InquiryResponse(
                id = inquiry.id!!,
                category = inquiry.category,
                title = inquiry.title,
                createdAt = inquiry.createdAt,
                updatedAt = inquiry.updatedAt,
            )
        }

        val total = inquiryRepository.countFiltered(category)
        val totalPages = (((total / size) + if (total % size > 0) 1 else 0)).toInt()
        val last = page * size >= total

        return PageResponse(
            content = inquiryResponses,
            page = page,
            size = size,
            totalElements = total,
            totalPages = totalPages,
            last = last,
        )
    }

    suspend fun getInquiry(inquiryId: Long) = inquiryRepository.findById(inquiryId)?.let { inquiry ->
        InquiryInfoResponse(
            id = inquiry.id!!,
            category = inquiry.category,
            title = inquiry.title,
            content = inquiry.content,
            userId = inquiry.userId,
            createdAt = inquiry.createdAt,
            updatedAt = inquiry.updatedAt,
        )
    } ?: throw CustomException(InquiryError.INQUIRY_NOT_FOUND)
}