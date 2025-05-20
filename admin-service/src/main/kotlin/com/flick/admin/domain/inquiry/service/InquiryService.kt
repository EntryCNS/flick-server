package com.flick.admin.domain.inquiry.service

import com.flick.admin.domain.inquiry.dto.response.InquiryDetailResponse
import com.flick.admin.domain.inquiry.dto.response.InquiryResponse
import com.flick.common.dto.Page
import com.flick.common.error.CustomException
import com.flick.domain.inquiry.enums.InquiryCategory
import com.flick.domain.inquiry.error.InquiryError
import com.flick.domain.inquiry.repository.InquiryRepository
import com.flick.domain.user.error.UserError
import com.flick.domain.user.repository.UserRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class InquiryService(
    private val inquiryRepository: InquiryRepository,
    private val userRepository: UserRepository,
) {
    suspend fun getInquiries(
        category: InquiryCategory? = null,
        page: Int,
        size: Int,
    ): Page<InquiryResponse> {
        val offset = (page - 1).coerceAtLeast(0) * size
        val (inquiries, total) = when (category) {
            null -> Pair(
                inquiryRepository.findAll(size, offset),
                inquiryRepository.count()
            )

            else -> Pair(
                inquiryRepository.findAllByCategory(category, size, offset),
                inquiryRepository.countByCategory(category)
            )
        }

        return Page.of(
            content = inquiries.map { inquiry ->
                InquiryResponse(
                    id = inquiry.id!!,
                    category = inquiry.category,
                    title = inquiry.title,
                    createdAt = inquiry.createdAt,
                    updatedAt = inquiry.updatedAt
                )
            }.toList(),
            pageNumber = page,
            pageSize = size,
            totalElements = total
        )
    }

    suspend fun getInquiry(inquiryId: Long): InquiryDetailResponse {
        return inquiryRepository.findById(inquiryId)
            ?.let { inquiry ->
                val user = userRepository.findById(inquiry.userId)
                    ?: throw CustomException(UserError.USER_NOT_FOUND)

                InquiryDetailResponse(
                    id = inquiry.id!!,
                    category = inquiry.category,
                    title = inquiry.title,
                    content = inquiry.content,
                    user = InquiryDetailResponse.User(
                        id = user.id!!,
                        name = user.name
                    ),
                    createdAt = inquiry.createdAt,
                    updatedAt = inquiry.updatedAt
                )
            }
            ?: throw CustomException(InquiryError.INQUIRY_NOT_FOUND)
    }
}