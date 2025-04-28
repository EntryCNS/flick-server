package com.flick.domain.booth.entity

import com.flick.domain.booth.enums.BoothStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("booths")
data class Booth(
    @Id
    val id: Long? = null,

    @Column("login_id")
    val loginId: String,

    @Column("password_hash")
    val passwordHash: String,

    @Column("status")
    val status: BoothStatus = BoothStatus.PENDING,

    @Column("name")
    val name: String,

    @Column("description")
    val description: String? = null,

    @Column("location")
    val location: String? = null,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)