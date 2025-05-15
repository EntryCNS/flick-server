package com.flick.admin.infra.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    companion object {
        private const val ADMIN = "ADMIN"
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOriginPatterns = listOf("*")
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true
        config.maxAge = 3600
        config.exposedHeaders = listOf("Authorization")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }

    @Bean
    fun configure(http: ServerHttpSecurity, jwtAuthenticationFilter: JwtAuthenticationFilter): SecurityWebFilterChain =
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
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
            .authorizeExchange {
                it
                    .pathMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                    .pathMatchers(HttpMethod.GET, "/ws/**").permitAll()

                    .pathMatchers(HttpMethod.POST, "/auth/login").permitAll()
                    .pathMatchers(HttpMethod.POST, "/auth/refresh").permitAll()

                    .anyExchange().hasRole(ADMIN)
            }
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
}