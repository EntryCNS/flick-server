package com.flick.admin.domain.transaction.controller

import com.flick.admin.domain.transaction.service.TransactionService
import com.flick.domain.transaction.enums.TransactionType
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/transactions")
class TransactionController(private val transactionService: TransactionService) {
    @GetMapping
    suspend fun getTransactions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam userId: Long?,
        @RequestParam type: TransactionType?,
        @RequestParam startDate: LocalDate?,
        @RequestParam endDate: LocalDate?
    ) = transactionService.getTransactions(page, size, userId, type, startDate, endDate)

    @GetMapping("/{transactionId}")
    suspend fun getTransaction(@PathVariable transactionId: Long) = transactionService.getTransaction(transactionId)
}