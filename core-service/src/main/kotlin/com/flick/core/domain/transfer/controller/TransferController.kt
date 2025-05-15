package com.flick.core.domain.transfer.controller

import com.flick.core.domain.transfer.dto.TransferRequest
import com.flick.core.domain.transfer.service.TransferService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transfer")
class TransferController(private val transferService: TransferService) {
    @PostMapping
    suspend fun transfer(@RequestBody request: TransferRequest) = transferService.transfer(request)
}