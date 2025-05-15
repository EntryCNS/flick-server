package com.flick.core.domain.transfer.service

import com.flick.common.error.CustomException
import com.flick.core.domain.transfer.dto.TransferRequest
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
class TransferService(
    private val securityHolder: SecurityHolder,
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
) {
    @Transactional(isolation = Isolation.SERIALIZABLE)
    suspend fun transfer(request: TransferRequest) {
        if (request.amount <= 0) throw CustomException(UserError.INVALID_AMOUNT)

        val userId = securityHolder.getUserId()
        if (userId == request.to) throw CustomException(UserError.SELF_TRANSFER_NOT_ALLOWED)

        val (firstId, secondId) = if (userId < request.to) {
            userId to request.to
        } else {
            request.to to userId
        }

        val firstUser = userRepository.findById(firstId) ?: throw CustomException(UserError.USER_NOT_FOUND)
        val secondUser = userRepository.findById(secondId) ?: throw CustomException(UserError.USER_NOT_FOUND)

        val (user, toUser) = if (userId == firstId) {
            firstUser to secondUser
        } else {
            secondUser to firstUser
        }

        if (user.balance < request.amount) throw CustomException(UserError.NOT_ENOUGH_BALANCE)

        val balanceAfter = user.balance - request.amount
        val toBalanceAfter = toUser.balance + request.amount

        userRepository.save(user.copy(balance = balanceAfter))
        userRepository.save(toUser.copy(balance = toBalanceAfter))

        transactionRepository.save(TransactionEntity(
            userId = userId,
            amount = request.amount,
            type = TransactionType.TRANSFER_OUT,
            memo = request.memo,
            balanceAfter = balanceAfter,
        ))

        transactionRepository.save(TransactionEntity(
            userId = request.to,
            amount = request.amount,
            type = TransactionType.TRANSFER_IN,
            memo = request.memo,
            balanceAfter = toBalanceAfter,
        ))
    }
}