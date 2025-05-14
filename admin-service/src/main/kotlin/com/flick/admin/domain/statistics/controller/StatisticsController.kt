package com.flick.admin.domain.statistics.controller

import com.flick.admin.domain.statistics.dto.StatisticeResponse
import com.flick.admin.domain.statistics.service.StatisticeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/admin/statistics")
@Tag(name = "Statistics", description = "통계 관련 API")
class StatisticsController(
    private val statisticeService: StatisticeService
) {
    @Operation(summary = "유저 거래 요약 정보 조회", description = "특정 유저의 충전 총액, 사용 총액, 현재 잔액을 조회합니다.")
    @GetMapping("/users/{userId}/transaction-summary")
    suspend fun getUserTransactionSummary(
        @PathVariable userId: Long
    ): StatisticeResponse {
        return statisticeService.getUserTransactionSummary(userId)
    }

    @Operation(summary = "전체 유저 거래 요약 정보 조회", description = "모든 유저의 충전 총액, 사용 총액, 현재 잔액의 합계를 조회합니다.")
    @GetMapping("/users/transaction-summary")
    suspend fun getAllUsersTransactionSummary(): StatisticeResponse {
        return statisticeService.getAllUsersTransactionSummary()
    }
}