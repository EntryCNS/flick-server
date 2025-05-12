package com.flick.admin.domain.user.service

import com.flick.admin.domain.user.dto.request.ChargeUserPointRequest
import com.flick.common.error.CustomException
import com.flick.core.infra.security.SecurityHolder
import com.flick.domain.transaction.entity.TransactionEntity
import com.flick.domain.transaction.enums.TransactionType
import com.flick.domain.transaction.repository.TransactionRepository
import com.flick.domain.user.error.UserError
import com.flick.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val securityHolder: SecurityHolder
){
    @Transactional(isolation = Isolation.SERIALIZABLE)
    suspend fun chargeUserPoint(request: ChargeUserPointRequest) {
        val user = userRepository.findById(request.userId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)
        val adminId = securityHolder.getUserId()

        val amount = request.amount
        val balanceAfter = user.balance + amount

        transactionRepository.save(TransactionEntity(
            userId = user.id!!,
            type = TransactionType.CHARGE,
            amount = amount,
            balanceAfter = balanceAfter,
            adminId = adminId
        ))
        userRepository.save(user.copy(
            balance = balanceAfter
        ))
    }
}