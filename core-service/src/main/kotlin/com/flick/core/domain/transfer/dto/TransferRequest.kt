package com.flick.core.domain.transfer.dto

data class TransferRequest(
    val to: Long,
    val amount: Long,
    val memo: String? = null
)