package com.flick.place.infra.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(private val jwtProvider: JwtProvider) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = jwtProvider.resolveToken(exchange.request) ?: return chain.filter(exchange)

        try {
            val boothId = jwtProvider.getBoothId(token)
            val role = jwtProvider.getRole(token)
            val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
            val authentication = UsernamePasswordAuthenticationToken(boothId, null, authorities)

            return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
        } catch (_: Exception) {
            return chain.filter(exchange)
        }
    }
}