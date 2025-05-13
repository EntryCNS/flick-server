package com.flick.admin.domain.transaction.service

import com.flick.admin.domain.transaction.dto.response.TransactionDetailResponse
import com.flick.admin.domain.transaction.dto.response.TransactionResponse
import com.flick.common.dto.Page
import com.flick.common.error.CustomException
import com.flick.domain.transaction.enums.TransactionType
import com.flick.domain.transaction.error.TransactionError
import com.flick.domain.transaction.repository.TransactionRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.LocalDate

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val transactionalOperator: TransactionalOperator
) {
    suspend fun getTransactions(
        page: Int,
        size: Int,
        userId: Long?,
        type: TransactionType?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Page<TransactionResponse> = transactionalOperator.executeAndAwait {
        val typeStr = type?.name
        val offset = (page - 1) * size

        val transactions = transactionRepository.findByFilters(
            userId, typeStr, startDate, endDate, size, offset
        ).map {
            TransactionResponse(
                id = it.id!!,
                userId = it.userId,
                type = it.type,
                amount = it.amount,
                balanceAfter = it.balanceAfter,
                createdAt = it.createdAt
            )
        }.toList()

        val totalElements = transactionRepository.countByFilters(userId, typeStr, startDate, endDate)

        Page.of(
            content = transactions,
            pageNumber = page,
            pageSize = size,
            totalElements = totalElements
        )
    }

    suspend fun getTransaction(transactionId: Long): TransactionDetailResponse = transactionalOperator.executeAndAwait {
        val transaction = transactionRepository.findById(transactionId)
            ?: throw CustomException(TransactionError.TRANSACTION_NOT_FOUND)

        TransactionDetailResponse(
            id = transaction.id!!,
            userId = transaction.userId,
            type = transaction.type,
            amount = transaction.amount,
            balanceAfter = transaction.balanceAfter,
            orderId = transaction.orderId,
            adminId = transaction.adminId,
            memo = transaction.memo,
            createdAt = transaction.createdAt
        )
    }
}