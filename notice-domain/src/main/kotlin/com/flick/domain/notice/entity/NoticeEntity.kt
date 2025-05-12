package com.flick.domain.notice.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("notices")
data class NoticeEntity(
    @Id
    val id: Long? = null,

    @Column("title")
    val title: String,

    @Column("content")
    val content: String,

    @Column("is_pinned")
    val isPinned: Boolean,

    @Column("author_id")
    val authorId: Long,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)