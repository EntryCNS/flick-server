package com.flick.domain.user.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("users")
data class UserEntity(
    @Id
    val id: Long? = null,

    @Column("dauth_id")
    val dAuthId: String,

    @Column("name")
    val name: String,

    @Column("email")
    val email: String? = null,

    @Column("grade")
    val grade: Int? = null,

    @Column("room")
    val room: Int? = null,

    @Column("number")
    val number: Int? = null,

    @Column("balance")
    val balance: Long = 0L,

    @Column("push_token")
    val pushToken: String? = null,

    @Column("profile_url")
    val profileUrl: String? = null,

    @Column("last_login_at")
    val lastLoginAt: LocalDateTime? = null,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)