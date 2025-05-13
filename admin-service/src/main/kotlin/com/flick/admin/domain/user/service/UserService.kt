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
import com.flick.domain.user.enums.UserRoleType
import com.flick.domain.user.error.UserError
import com.flick.domain.user.repository.UserRepository
import kotlinx.coroutines.flow.toList
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
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
        val users = userRepository.findAllByFilters(name, grade, room, role, size, offset).toList()

        return PageResponse(
            content = users.map { user ->
                UserResponse(
                    id = user.id,
                    name = user.name,
                    role = if (UserRoleType.TEACHER in user.roles)
                        UserRoleType.TEACHER else UserRoleType.STUDENT,
                    grade = user.grade,
                    room = user.room,
                    number = user.number,
                    balance = user.balance
                )
            },
            page = page,
            size = size,
            totalElements = users.firstOrNull()?.totalCount ?: 0,
            totalPages = ((users.firstOrNull()?.totalCount ?: 0) + size - 1) / size,
            last = users.isEmpty() || users.last().rowNum >= (users.firstOrNull()?.totalCount ?: 0)
        )
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    suspend fun chargeUserPoint(userId: Long, request: ChargeUserPointRequest) {
        val user = userRepository.findOneByIdForUpdate(userId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        val balanceAfter = user.balance + request.amount

        userRepository.save(user.copy(balance = balanceAfter))
            .also { savedUser ->
                transactionRepository.save(
                    TransactionEntity(
                        userId = savedUser.id!!,
                        type = TransactionType.CHARGE,
                        amount = request.amount,
                        balanceAfter = balanceAfter,
                        adminId = securityHolder.getAdminId()
                    )
                )
            }
            .also { savedUser ->
                kafkaTemplate.send(
                    "point-charged",
                    objectMapper.writeValueAsString(
                        PointChargedEventDto(
                            userId = savedUser.id!!,
                            amount = request.amount,
                            balanceAfter = balanceAfter
                        )
                    )
                )
            }
    }
}