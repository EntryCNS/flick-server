package com.flick.admin.domain.inquiry.service

import com.flick.admin.domain.inquiry.dto.response.InquiryDetailResponse
import com.flick.admin.domain.inquiry.dto.response.InquiryResponse
import com.flick.common.dto.PageResponse
import com.flick.common.error.CustomException
import com.flick.domain.inquiry.enums.InquiryCategory
import com.flick.domain.inquiry.error.InquiryError
import com.flick.domain.inquiry.repository.InquiryRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class InquiryService(
    private val inquiryRepository: InquiryRepository
) {
    suspend fun getInquiries(
        category: InquiryCategory? = null,
        page: Int,
        size: Int,
    ): PageResponse<InquiryResponse> {
        val offset = (page - 1).coerceAtLeast(0) * size
        val (inquiries, total) = when (category) {
            null -> Pair(
                inquiryRepository.findAll(page, size),
                inquiryRepository.count()
            )
            else -> Pair(
                inquiryRepository.findAllByCategory(category, size, offset),
                inquiryRepository.countByCategory(category)
            )
        }

        return PageResponse(
            content = inquiries.map { inquiry ->
                InquiryResponse(
                    id = inquiry.id!!,
                    category = inquiry.category,
                    title = inquiry.title,
                    createdAt = inquiry.createdAt,
                    updatedAt = inquiry.updatedAt
                )
            }.toList(),
            page = page,
            size = size,
            totalElements = total,
            totalPages = (total + size - 1) / size,
            last = page * size >= total
        )
    }

    suspend fun getInquiry(inquiryId: Long): InquiryDetailResponse =
        (inquiryRepository.findById(inquiryId) ?: throw CustomException(InquiryError.INQUIRY_NOT_FOUND))
            .let { inquiry ->
                InquiryDetailResponse(
                    id = inquiry.id!!,
                    category = inquiry.category,
                    title = inquiry.title,
                    content = inquiry.content,
                    userId = inquiry.userId,
                    createdAt = inquiry.createdAt,
                    updatedAt = inquiry.updatedAt
                )
            }
}