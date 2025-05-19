package com.flick.place.infra.security

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
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {
    companion object {
        private const val BOOTH = "BOOTH"
        private const val KIOSK = "KIOSK"
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
    fun configure(http: ServerHttpSecurity): SecurityWebFilterChain = http
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
                .pathMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                .pathMatchers(HttpMethod.GET, "/ws/**").permitAll()

                .pathMatchers(HttpMethod.GET, "/orders").authenticated()
                .pathMatchers(HttpMethod.GET, "/orders/{orderId}").authenticated()
                .pathMatchers(HttpMethod.POST, "/orders").authenticated()

                .pathMatchers(HttpMethod.GET, "/booths/check").permitAll()
                .pathMatchers(HttpMethod.GET, "/booths/sales").hasRole(BOOTH)

                .pathMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .pathMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .pathMatchers(HttpMethod.POST, "/auth/refresh").permitAll()

                .pathMatchers(HttpMethod.POST, "/kiosks/login").permitAll()
                .pathMatchers(HttpMethod.POST, "/kiosks/generate").hasRole(BOOTH)
                .pathMatchers(HttpMethod.POST, "/kiosks/register").permitAll()

                .pathMatchers(HttpMethod.GET, "/products").authenticated()
                .pathMatchers(HttpMethod.GET, "/products/available").authenticated()
                .pathMatchers(HttpMethod.GET, "/products/{productId}").authenticated()
                .pathMatchers(HttpMethod.POST, "/products").authenticated()
                .pathMatchers(HttpMethod.PATCH, "/products/{productId}").authenticated()
                .pathMatchers(HttpMethod.DELETE, "/products/{productId}").authenticated()

                .anyExchange().authenticated()
        }
        .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .build()

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}