package com.flick.core.infra.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    companion object {
        private const val STUDENT = "STUDENT"
        private const val TEACHER = "TEACHER"
    }

    @Bean
    fun roleHierarchy(): RoleHierarchy = RoleHierarchyImpl.fromHierarchy("$STUDENT > $TEACHER")

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun configure(http: ServerHttpSecurity, jwtAuthenticationFilter: JwtAuthenticationFilter) = http
        .cors { it.disable() }
        .httpBasic { it.disable() }
        .formLogin { it.disable() }
        .csrf { it.disable() }
        .logout { it.disable() }
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .exceptionHandling {
            it.authenticationEntryPoint { exchange, _ ->
                Mono.fromRunnable {
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                }
            }
            it.accessDeniedHandler { exchange, _ ->
                Mono.fromRunnable {
                    exchange.response.statusCode = HttpStatus.FORBIDDEN
                }
            }
        }
        .authorizeExchange { it
            .pathMatchers(HttpMethod.GET ,"/swagger-ui/**", "/v3/api-docs/**").permitAll()

            .pathMatchers(HttpMethod.GET, "/ws/**").permitAll()

            .pathMatchers(HttpMethod.POST, "/auth/login").permitAll()
            .pathMatchers(HttpMethod.POST, "/auth/refresh").permitAll()

            .pathMatchers(HttpMethod.GET, "/users/me/balance").authenticated()

            .pathMatchers(HttpMethod.POST, "/payments/requests/confirm").authenticated()

            .pathMatchers(HttpMethod.GET, "/notices").authenticated()

            .pathMatchers(HttpMethod.GET, "/booths").authenticated()

            .pathMatchers(HttpMethod.GET, "/transactions/my").authenticated()
            .pathMatchers(HttpMethod.GET, "/transactions/{transactionId}").authenticated()

            .anyExchange().authenticated()
        }
        .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .build()
}