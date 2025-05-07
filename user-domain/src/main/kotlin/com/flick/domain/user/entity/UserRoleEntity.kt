package com.flick.domain.payment.entity

import com.flick.domain.payment.enums.UserRoleType
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("user_roles")
data class UserRoleEntity(
    @Id
    val id: Long? = null,

    @Column("user_id")
    val userId: Long,

    @Column("role")
    val role: UserRoleType,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)