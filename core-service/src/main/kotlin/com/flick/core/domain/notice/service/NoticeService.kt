package com.flick.core.domain.notice.service

import com.flick.common.error.CustomException
import com.flick.core.domain.notice.dto.response.NoticeResponse
import com.flick.domain.notice.error.NoticeError
import com.flick.domain.notice.repository.NoticeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service

@Service
class NoticeService(private val noticeRepository: NoticeRepository) {
    suspend fun getNotices(): Flow<NoticeResponse> {
        val notices = noticeRepository.findAll()

        return notices.map {
            NoticeResponse(
                id = it.id!!,
                title = it.title,
                isPinned = it.isPinned,
                content = it.content,
                createdAt = it.createdAt,
            )
        }
    }

    suspend fun getNotice(noticeId: Long): NoticeResponse {
        val notice = noticeRepository.findById(noticeId)
            ?: throw CustomException(NoticeError.NOTICE_NOT_FOUND)

        return NoticeResponse(
            id = notice.id!!,
            title = notice.title,
            isPinned = notice.isPinned,
            content = notice.content,
            createdAt = notice.createdAt,
        )
    }
}