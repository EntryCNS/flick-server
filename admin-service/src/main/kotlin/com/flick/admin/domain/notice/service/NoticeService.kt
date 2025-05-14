package com.flick.admin.domain.notice.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.flick.admin.domain.notice.dto.NoticeCreatedEvent
import com.flick.admin.domain.notice.dto.request.CreateNoticeRequest
import com.flick.admin.domain.notice.dto.request.UpdateNoticeRequest
import com.flick.admin.domain.notice.dto.response.NoticeResponse
import com.flick.admin.infra.security.SecurityHolder
import com.flick.common.error.CustomException
import com.flick.domain.notice.entity.NoticeEntity
import com.flick.domain.notice.error.NoticeError
import com.flick.domain.notice.repository.NoticeRepository
import com.flick.domain.user.error.UserError
import com.flick.domain.user.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class NoticeService(
    private val noticeRepository: NoticeRepository,
    private val securityHolder: SecurityHolder,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val kafkaTemplate: KafkaTemplate<Any, Any>,
    private val transactionalOperator: TransactionalOperator
) {
    suspend fun getNotices(): Flow<NoticeResponse> {
        val notices = noticeRepository.findAll()

        return notices.map {
            NoticeResponse(
                id = it.id!!,
                title = it.title,
                content = it.content,
                isPinned = it.isPinned,
                author = NoticeResponse.Author(
                    name = getAuthor(it.authorId).name
                ),
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
            content = notice.content,
            isPinned = notice.isPinned,
            author = NoticeResponse.Author(
                name = getAuthor(notice.authorId).name
            ),
            createdAt = notice.createdAt,
        )
    }

    suspend fun createNotice(request: CreateNoticeRequest) {
        val adminId = securityHolder.getAdminId()

        broadcastNotice(
            noticeRepository.save(
                NoticeEntity(
                    title = request.title,
                    content = request.content,
                    authorId = adminId,
                    isPinned = request.isPinned
                )
            )
        )
    }

    suspend fun updateNotice(noticeId: Long, request: UpdateNoticeRequest) {
        val adminId = securityHolder.getAdminId()

        transactionalOperator.executeAndAwait {
            val notice = noticeRepository.findById(noticeId)
                ?: throw CustomException(NoticeError.NOTICE_NOT_FOUND)

            if (notice.authorId != adminId) {
                throw CustomException(NoticeError.NOTICE_FORBIDDEN)
            }

            noticeRepository.save(
                notice.copy(
                    title = request.title ?: notice.title,
                    content = request.content ?: notice.content,
                )
            )
        }
    }

    suspend fun deleteNotice(noticeId: Long) {
        val adminId = securityHolder.getAdminId()

        transactionalOperator.executeAndAwait {
            val notice = noticeRepository.findById(noticeId)
                ?: throw CustomException(NoticeError.NOTICE_NOT_FOUND)

            if (notice.authorId != adminId) {
                throw CustomException(NoticeError.NOTICE_FORBIDDEN)
            }

            noticeRepository.delete(notice)
        }
    }

    private suspend fun getAuthor(authorId: Long) =
        userRepository.findById(authorId) ?: throw CustomException(UserError.USER_NOT_FOUND)

    private suspend fun broadcastNotice(notice: NoticeEntity) {
        userRepository.findAll().collect {
            val event = NoticeCreatedEvent(
                id = notice.id!!,
                userId = it.id!!,
                title = notice.title,
            )
            kafkaTemplate.send("notice-created", objectMapper.writeValueAsString(event))
        }
    }
}