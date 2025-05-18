package com.flick.domain.booth.entity

import com.flick.domain.booth.enums.BoothStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("booths")
data class BoothEntity(
    @Id
    val id: Long? = null,

    @Column("username")
    val username: String,

    @Column("password_hash")
    val passwordHash: String,

    @Column("name")
    val name: String,

    @Column("description")
    val description: String? = null,

    @Column("image_url")
    val imageUrl: String? = null,

    @Column("status")
    val status: BoothStatus = BoothStatus.PENDING,

    @Column("total_sales")
    val totalSales: Long = 0L,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)