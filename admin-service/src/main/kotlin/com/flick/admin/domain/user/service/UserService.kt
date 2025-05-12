package com.flick.admin.domain.user.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.flick.admin.domain.user.dto.PointChargedEventDto
import com.flick.admin.domain.user.dto.request.ChargeUserPointRequest
import com.flick.admin.domain.user.dto.response.UserResponse
import com.flick.admin.infra.security.SecurityHolder
import com.flick.common.dto.PageResponse
import com.flick.common.error.CustomException
import com.flick.domain.transaction.entity.TransactionEntity
import com.flick.domain.transaction.enums.TransactionType
import com.flick.domain.transaction.repository.TransactionRepository
import com.flick.domain.user.entity.UserRoleEntity
import com.flick.domain.user.enums.UserRoleType
import com.flick.domain.user.error.UserError
import com.flick.domain.user.repository.UserRepository
import com.flick.domain.user.repository.UserRoleRepository
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val transactionalRepository: TransactionRepository,
    private val securityHolder: SecurityHolder,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Qualifier("writeTx") private val writeTx: TransactionalOperator,
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

        val userIds = userList.mapNotNull { it.id }

        val userRolesMap: Map<Long, List<UserRoleEntity>> =
            userRoleRepository.findAllByUserIdIn(userIds)
                .toList()
                .groupBy { it.userId }

        val userResponses = userList.map { user ->
            val roles = userRolesMap[user.id]
            val isTeacher = roles!!.any { it.role == UserRoleType.TEACHER }

            UserResponse(
                id = user.id!!,
                name = user.name,
                role = if (isTeacher) UserRoleType.TEACHER else UserRoleType.STUDENT,
                grade = user.grade,
                room = user.room,
                number = user.number,
                balance = user.balance,
            )
        }

        val total = userRepository.countFiltered(name, grade, room, role)
        val totalPages = ((total / size) + if (total % size > 0) 1 else 0).toInt()
        val last = page * size >= total

        return PageResponse(
            content = userResponses,
            page = page,
            size = size,
            totalElements = total,
            totalPages = totalPages,
            last = last,
        )
    }

    suspend fun chargeUserPoint(userId: Long, request: ChargeUserPointRequest) {
        val adminId = securityHolder.getAdminId()
        val amount = request.amount

        writeTx.executeAndAwait {
            val user = userRepository.findByIdForUpdate(userId)
                ?: throw CustomException(UserError.USER_NOT_FOUND)

            val balanceAfter = user.balance + amount

            transactionalRepository.save(
                TransactionEntity(
                    userId = user.id!!,
                    type = TransactionType.CHARGE,
                    amount = amount,
                    balanceAfter = balanceAfter,
                    adminId = adminId,
                )
            )

            userRepository.save(user.copy(balance = balanceAfter))

            val event = PointChargedEventDto(
                userId = user.id!!,
                amount = amount,
                balanceAfter = balanceAfter,
            )

            val eventJson = objectMapper.writeValueAsString(event)
            kafkaTemplate.send("point-charged", eventJson)
        }
    }
}