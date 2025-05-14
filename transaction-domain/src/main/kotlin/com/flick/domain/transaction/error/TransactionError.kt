package com.flick.domain.transaction.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class TransactionError(override val status: HttpStatus, override val message: String) : CustomError {
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Transaction not found"),
}