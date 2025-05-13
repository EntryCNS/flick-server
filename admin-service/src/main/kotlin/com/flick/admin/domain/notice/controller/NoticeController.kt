package com.flick.core.domain.notice.controller

import com.flick.core.domain.notice.service.NoticeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notices")
class NoticeController(private val noticeService: NoticeService) {
    @GetMapping
    suspend fun getNotices() = noticeService.getNotices()

    @GetMapping("/{noticeId}")
    suspend fun getNotice(@PathVariable noticeId: Long) = noticeService.getNotice(noticeId)
}