package com.flick.admin.infra.security

import com.flick.common.utils.logger
import com.flick.domain.user.repository.UserRoleRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val userRoleRepository: UserRoleRepository,
) : WebFilter {
    private val log = logger()

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        log.info("request: ${exchange.request.method} ${exchange.request.path}")
        val token = jwtProvider.resolveToken(exchange.request) ?: return chain.filter(exchange)

        return mono {
            try {
                val userId = jwtProvider.getUserId(token)
                log.info("$userId is authenticated")
                val roles = userRoleRepository.findAllByUserId(userId).toList()
                log.info("roles: $roles")

                val authorities = roles.map { SimpleGrantedAuthority("ROLE_${it.role.name}") }
                val authentication = UsernamePasswordAuthenticationToken(userId, null, authorities)

                chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                    .awaitSingleOrNull()
            } catch (e: Exception) {
                chain.filter(exchange).awaitSingleOrNull()
            }
        }
    }
}