package com.flick.admin.domain.notice.controller

import com.flick.admin.domain.notice.dto.request.CreateNoticeRequest
import com.flick.admin.domain.notice.dto.request.UpdateNoticeRequest
import com.flick.admin.domain.notice.service.NoticeService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/notices")
class NoticeController(private val noticeService: NoticeService) {
    @GetMapping
    suspend fun getNotices() = noticeService.getNotices()

    @GetMapping("/{noticeId}")
    suspend fun getNotice(@PathVariable noticeId: Long) = noticeService.getNotice(noticeId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createNotice(@RequestBody request: CreateNoticeRequest) = noticeService.createNotice(request)

    @PatchMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun updateNotice(@PathVariable noticeId: Long, @RequestBody request: UpdateNoticeRequest) =
        noticeService.updateNotice(noticeId, request)

    @DeleteMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteNotice(@PathVariable noticeId: Long) = noticeService.deleteNotice(noticeId)
}