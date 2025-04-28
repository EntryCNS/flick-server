package com.flick.domain.user.entity

import com.flick.domain.user.enums.UserRole
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("users")
data class User(
    @Id
    val id: Long? = null,

    @Column("unique_id")
    val uniqueId: String? = null,

    @Column("login_id")
    val loginId: String? = null,

    @Column("password_hash")
    val passwordHash: String? = null,

    @Column("name")
    val name: String,

    @Column("email")
    val email: String? = null,

    @Column("device_token")
    val deviceToken: String? = null,

    @Column("point")
    val point: Long = 0,

    @Column("grade")
    val grade: Int? = null,

    @Column("room")
    val room: Int? = null,

    @Column("number")
    val number: Int? = null,

    @Column("profile_image")
    val profileImage: String? = null,

    @Column("role")
    val role: UserRole = UserRole.STUDENT,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)