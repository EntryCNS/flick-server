package com.flick.place.infra.security

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component

@Component
class JwtHolder {
    suspend fun getBoothId(): Long {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication.principal as JwtPayload }
            .map { it.id }
            .awaitSingle()
    }

    suspend fun getSessionId(): Long? {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication.principal as JwtPayload }
            .map { it.sessionId }
            .awaitSingle()
    }

    suspend fun getType(): JwtType {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication.principal as JwtPayload }
            .map { it.type }
            .awaitSingle()
    }
}