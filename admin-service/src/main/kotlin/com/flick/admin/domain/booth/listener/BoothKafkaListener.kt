package com.flick.admin.domain.booth.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.flick.admin.domain.booth.dto.response.BoothSalesUpdatedDto
import com.flick.admin.domain.booth.service.BoothService
import com.flick.common.utils.logger
import kotlinx.coroutines.runBlocking
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class BoothKafkaListener(
    private val objectMapper: ObjectMapper,
    private val boothService: BoothService,
) {
    private val log = logger()

    @KafkaListener(topics = ["booth-sales-updated"], groupId = "admin-group")
    fun handleBoothSalesUpdated(message: String) {
        try {
            val event = objectMapper.readValue<BoothSalesUpdatedDto>(message)
            log.info("Received booth sales update: boothId=${event.boothId}, totalSales=${event.totalSales}")

            runBlocking {
                boothService.publishRanking()
            }
        } catch (e: Exception) {
            log.error("Failed to process booth-sales-updated: $message", e)
        }
    }
}