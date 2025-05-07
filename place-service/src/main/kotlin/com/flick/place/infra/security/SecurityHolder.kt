package com.flick.place.infra.security

import com.flick.common.error.CustomException
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityHolder {
    suspend fun getBoothId(): Long {
        return ReactiveSecurityContextHolder.getContext()
            .mapNotNull { it.authentication.principal as? Long }
            .awaitSingleOrNull()
            ?: throw CustomException(JwtError.INVALID_TOKEN)
    }
}