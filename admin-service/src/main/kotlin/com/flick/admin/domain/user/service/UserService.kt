package com.flick.admin.domain.user.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.flick.admin.domain.user.dto.PointChargedEventDto
import com.flick.admin.domain.user.dto.request.ChargeUserPointRequest
import com.flick.admin.domain.user.dto.response.UserInfoResponse
import com.flick.admin.domain.user.dto.response.UserResponse
import com.flick.admin.infra.security.SecurityHolder
import com.flick.common.dto.PageResponse
import com.flick.common.error.CustomException
import com.flick.domain.transaction.entity.TransactionEntity
import com.flick.domain.transaction.enums.TransactionType
import com.flick.domain.transaction.repository.TransactionRepository
import com.flick.domain.user.enums.UserRoleType
import com.flick.domain.user.error.UserError
import com.flick.domain.user.repository.UserRepository
import com.flick.domain.user.repository.UserRoleRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val securityHolder: SecurityHolder,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val transactionRepository: TransactionRepository,
) {
    suspend fun getUsers(
        name: String?,
        grade: Int?,
        room: Int?,
        role: UserRoleType?,
        page: Int,
        size: Int,
    ): PageResponse<UserResponse> {
        val offset = (page - 1).coerceAtLeast(0) * size
        val userList = userRepository.findFiltered(name, grade, room, role, size, offset).toList()
        val total = userRepository.countFiltered(name, grade, room, role)

        val userRolesMap = userRoleRepository.findAllByUserIdIn(userList.mapNotNull { it.id })
            .toList()
            .groupBy { it.userId }

        return PageResponse(
            content = userList.map { user ->
                val isTeacher = userRolesMap[user.id]?.any { it.role == UserRoleType.TEACHER } ?: false
                UserResponse(
                    id = user.id!!,
                    name = user.name,
                    role = if (isTeacher) UserRoleType.TEACHER else UserRoleType.STUDENT,
                    grade = user.grade,
                    room = user.room,
                    number = user.number,
                    balance = user.balance,
                )
            },
            page = page,
            size = size,
            totalElements = total,
            totalPages = ((total + size - 1) / size),
            last = page * size >= total
        )
    }

    suspend fun getUser(userId: Long) = userRepository.findById(userId)?.let { user ->
        val isTeacher = userRoleRepository.findAllByUserId(user.id!!)
            .firstOrNull { it.role == UserRoleType.TEACHER } != null

        UserInfoResponse(
            id = user.id!!,
            name = user.name,
            email = user.email,
            role = if (isTeacher) UserRoleType.TEACHER else UserRoleType.STUDENT,
            grade = user.grade,
            room = user.room,
            number = user.number,
            balance = user.balance,
            profileUrl = user.profileUrl,
            lastLoginAt = user.lastLoginAt,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
        )
    } ?: throw CustomException(UserError.USER_NOT_FOUND)

    @Transactional(isolation = Isolation.SERIALIZABLE)
    suspend fun chargeUserPoint(userId: Long, request: ChargeUserPointRequest) {
        val user = userRepository.findById(userId) ?: throw CustomException(UserError.USER_NOT_FOUND)
        val balanceAfter = user.balance + request.amount

        transactionRepository.save(TransactionEntity(
            userId = user.id!!,
            type = TransactionType.CHARGE,
            amount = request.amount,
            balanceAfter = balanceAfter,
            adminId = securityHolder.getAdminId()
        ))

        userRepository.save(user.copy(balance = balanceAfter))

        PointChargedEventDto(
            userId = user.id!!,
            amount = request.amount,
            balanceAfter = balanceAfter
        ).let {
            kafkaTemplate.send("point-charged", objectMapper.writeValueAsString(it))
        }
    }
}