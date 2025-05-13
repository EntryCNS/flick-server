package com.flick.core.domain.notice.controller

import com.flick.admin.domain.notice.dto.request.CreateNoticeRequest
import com.flick.admin.domain.notice.dto.request.UpdateNoticeRequest
import com.flick.core.domain.notice.service.NoticeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notices")
class NoticeController(private val noticeService: NoticeService) {
    @GetMapping
    suspend fun getNotices() = noticeService.getNotices()

    @GetMapping("/{noticeId}")
    suspend fun getNotice(@PathVariable noticeId: Long) = noticeService.getNotice(noticeId)

    @PostMapping
    suspend fun createNotice(@RequestBody request: CreateNoticeRequest): ResponseEntity<Void> {
        noticeService.createNotice(request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PutMapping("/{noticeId}")
    suspend fun updateNotice(@PathVariable noticeId: Long,
                             @RequestBody request: UpdateNoticeRequest): ResponseEntity<Void> {
        noticeService.updateNotice(noticeId,request)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @DeleteMapping("/{noticeId}")
    suspend fun deleteNotice(@PathVariable noticeId: Long): ResponseEntity<Void> {
        noticeService.deleteNotice(noticeId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}