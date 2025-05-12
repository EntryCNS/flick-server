package com.flick.domain.inquiry.entity

import com.flick.domain.inquiry.enums.InquiryCategory
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("inquiries")
data class InquiryEntity(
    @Id
    val id: Long? = null,

    @Column("category")
    val category: InquiryCategory,

    @Column("title")
    val title: String,

    @Column("content")
    val content: String,

    @Column("user_id")
    val userId: Long,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)