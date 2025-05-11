package com.flick.admin.infra.transaction

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.support.DefaultTransactionDefinition

@Configuration
class TransactionConfig(private val transactionManager: ReactiveTransactionManager) {
    @Bean("readOnlyTx")
    fun readOnlyTransactionalOperator(): TransactionalOperator {
        val def = DefaultTransactionDefinition().apply {
            isReadOnly = true
            propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        }
        return TransactionalOperator.create(transactionManager, def)
    }

    @Bean("writeTx")
    fun writeTransactionalOperator(): TransactionalOperator {
        val def = DefaultTransactionDefinition().apply {
            isReadOnly = false
            propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        }
        return TransactionalOperator.create(transactionManager, def)
    }
}