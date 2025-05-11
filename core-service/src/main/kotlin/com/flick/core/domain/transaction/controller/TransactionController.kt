package com.flick.core.domain.transaction.controller

import com.flick.core.domain.transaction.service.TransactionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transactions")
class TransactionController(private val transactionService: TransactionService) {
    @GetMapping("/my")
    suspend fun getMyTransactions() = transactionService.getMyTransactions()

    @GetMapping("/{transactionId}")
    suspend fun getTransaction(@PathVariable transactionId: Long) = transactionService.getTransaction(transactionId)
}