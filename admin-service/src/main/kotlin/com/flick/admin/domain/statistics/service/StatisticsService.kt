package com.flick.admin.domain.statistics.service

import com.flick.admin.domain.statistics.dto.StatisticsResponse
import com.flick.domain.transaction.enums.TransactionType
import com.flick.domain.transaction.repository.TransactionRepository
import com.flick.domain.user.repository.UserRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class StatisticsService(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend fun getStatistics(): StatisticsResponse {
        val allUsers = userRepository.findAll().toList()

        val allTransactions = transactionRepository.findAll().toList()

        val totalCharged = allTransactions
            .filter { it.type == TransactionType.CHARGE }
            .sumOf { it.amount }

        val totalUsed = allTransactions
            .filter { it.type == TransactionType.PAYMENT }
            .sumOf { it.amount }

        val totalBalance = allUsers
            .sumOf { it.balance }

        return StatisticsResponse(
            totalCharge = totalCharged,
            totalUsed = totalUsed,
            totalBalance = totalBalance
        )
    }
}