package com.flick.domain.notice.repository

import com.flick.domain.notice.entity.NoticeEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface NoticeRepository : CoroutineCrudRepository<NoticeEntity, Long> {
}