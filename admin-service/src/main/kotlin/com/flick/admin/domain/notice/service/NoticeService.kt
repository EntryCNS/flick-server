package com.flick.admin.domain.notice.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.flick.admin.domain.notice.dto.NoticeCreatedEvent
import com.flick.admin.domain.notice.dto.request.CreateNoticeRequest
import com.flick.admin.domain.notice.dto.request.UpdateNoticeRequest
import com.flick.admin.infra.security.SecurityHolder
import com.flick.common.error.CustomException
import com.flick.core.domain.notice.dto.response.NoticeResponse
import com.flick.domain.notice.entity.NoticeEntity
import com.flick.domain.notice.error.NoticeError
import com.flick.domain.notice.repository.NoticeRepository
import com.flick.domain.user.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class NoticeService(
    private val noticeRepository: NoticeRepository,
    private val securityHolder: SecurityHolder,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val kafkaTemplate: KafkaTemplate<Any, Any>
) {
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

    suspend fun createNotice(request: CreateNoticeRequest) {
        val adminId = securityHolder.getAdminId()
        val notice = noticeRepository.save(
            NoticeEntity(
                title = request.title,
                content = request.content,
                authorId = adminId,
                isPinned = request.isPinned
            )
        )

        broadcastNotice(notice)
    }

    suspend fun updateNotice(noticeId: Long, request: UpdateNoticeRequest) {
        val adminId = securityHolder.getAdminId()
        val notice = noticeRepository.findById(noticeId)
            ?: throw CustomException(NoticeError.NOTICE_NOT_FOUND)

        if (notice.authorId != adminId) {
            throw CustomException(NoticeError.NOTICE_FORBIDDEN)
        }

        val updatedNotice = notice.copy(
            title = request.title ?: notice.title,
            content = request.content ?: notice.content,
        )

        noticeRepository.save(updatedNotice)
    }

    suspend fun deleteNotice(noticeId: Long) {
        val adminId = securityHolder.getAdminId()
        val notice = noticeRepository.findById(noticeId)
            ?: throw CustomException(NoticeError.NOTICE_NOT_FOUND)

        if (notice.authorId != adminId) {
            throw CustomException(NoticeError.NOTICE_FORBIDDEN)
        }

        noticeRepository.delete(notice)
    }

    private suspend fun broadcastNotice(notice: NoticeEntity) {
        val users = userRepository.findAll()

        users.collect {
            val event = NoticeCreatedEvent(
                id = notice.id!!,
                userId = it.id!!,
                title = notice.title,
            )
            val eventJson = objectMapper.writeValueAsString(event)
            kafkaTemplate.send("notice-created", eventJson)
        }
    }
}