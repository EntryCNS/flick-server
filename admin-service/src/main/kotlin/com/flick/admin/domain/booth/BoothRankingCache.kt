package com.flick.admin.domain.booth

import com.flick.admin.domain.booth.dto.response.BoothRankingResponse
import org.springframework.stereotype.Component

@Component
class BoothRankingCache {
    var previous: List<BoothRankingResponse> = emptyList()
}