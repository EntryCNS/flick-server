package com.flick.admin.domain.statistics.service

import com.flick.domain.transaction.repository.TransactionRepository
import com.flick.domain.user.repository.UserRepository
import org.springframework.transaction.reactive.TransactionalOperator

class StatisticeService(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionalOperator: TransactionalOperator,
) {
    suspend fun getUserTransactionSummary(userId: Long): UserTransactionSummaryResponse = transactionalOperator.executeAndAwait {

        val user = userRepository.findById(userId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        // 해당 유저의 모든 거래 내역 조회
        val transactions = transactionRepository.findAllByUserId(userId).toList()

        // 충전한 총 금액 계산 (CHARGE 타입의 트랜잭션들의 금액 합계)
        val totalCharged = transactions
            .filter { it.type == TransactionType.CHARGE }
            .sumOf { it.amount }

        // 사용한 총 금액 계산 (USAGE 타입의 트랜잭션들의 금액 합계)
        val totalUsed = transactions
            .filter { it.type == TransactionType.USAGE }
            .sumOf { it.amount }

        // 현재 잔액 (유저 엔티티에 저장된 값 사용)
        val currentBalance = user.balance

        // 응답 객체 생성
        UserTransactionSummaryResponse(
            userId = userId,
            userName = user.name,
            totalCharged = totalCharged,
            totalUsed = totalUsed,
            currentBalance = currentBalance
        )
    }
    suspend fun getAllUsersTransactionSummary(): UserTransactionSummaryResponse = transactionalOperator.executeAndAwait {

        val allTransactions = transactionRepository.findAll().toList()


        val totalCharged = allTransactions
            .filter { it.type == TransactionType.CHARGE }
            .sumOf { it.amount }


        val totalUsed = allTransactions
            .filter { it.type == TransactionType.USAGE }
            .sumOf { it.amount }

        // 전체 잔액
        val currentBalance = totalCharged - totalUsed

        // 응답 객체 생성 (전체 통계이므로 userId와 userName은 null로 설정)
        UserTransactionSummaryResponse(
            userId = null,
            userName = "전체 통계",
            totalCharged = totalCharged,
            totalUsed = totalUsed,
            currentBalance = currentBalance
        )
    }
}