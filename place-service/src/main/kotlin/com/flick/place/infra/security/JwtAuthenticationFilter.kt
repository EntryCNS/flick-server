package com.flick.place.infra.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
): WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = jwtProvider.extractToken(exchange.request) ?: return chain.filter(exchange)

        return try {
            if (!jwtProvider.validateToken(token)) {
                return chain.filter(exchange)
            }

            val payload = jwtProvider.getPayload(token)
            val authorities = listOf(SimpleGrantedAuthority("ROLE_${payload.type.name}"))
            val authentication = UsernamePasswordAuthenticationToken(payload, null, authorities)
            val context = SecurityContextImpl(authentication)

            chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)))
        } catch (e: Exception) {
            chain.filter(exchange)
        }
    }
}