package com.flick.place.infra.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
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
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOriginPatterns = listOf("*")
        config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }

    @Bean
    fun configure(http: ServerHttpSecurity): SecurityWebFilterChain = http
        .csrf { it.disable() }
        .httpBasic { it.disable() }
        .formLogin { it.disable() }
        .cors { it.configurationSource(corsConfigurationSource()) }
        .exceptionHandling {
            it.authenticationEntryPoint { exchange, _ ->
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                exchange.response.headers.add(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                Mono.empty()
            }
            it.accessDeniedHandler { exchange, _ ->
                exchange.response.statusCode = HttpStatus.FORBIDDEN
                Mono.empty()
            }
        }
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .authorizeExchange { it
            // 인증 관련 엔드포인트
            .pathMatchers(HttpMethod.POST, "/booths/login").permitAll()
            .pathMatchers(HttpMethod.POST, "/booths/register").permitAll()
            .pathMatchers(HttpMethod.GET, "/booths/me").permitAll()

            // 시스템 엔드포인트
            .pathMatchers(HttpMethod.GET, "/actuator/**").permitAll()
            .pathMatchers(HttpMethod.GET, "/health").permitAll()
            .pathMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui/**").permitAll()

            // 키오스크 세션 관리
            .pathMatchers(HttpMethod.POST, "/kiosks/connect").hasRole("BOOTH")
            .pathMatchers(HttpMethod.POST, "/kiosks/verify").permitAll()
            .pathMatchers(HttpMethod.GET, "/kiosks/sessions").hasRole("BOOTH")
            .pathMatchers(HttpMethod.GET, "/kiosks/sessions/**").hasRole("BOOTH")
            .pathMatchers(HttpMethod.PATCH, "/kiosks/sessions/*/disconnect").hasRole("BOOTH")

            // 상품 관리
            .pathMatchers(HttpMethod.GET, "/products").hasAnyRole("BOOTH", "KIOSK")
            .pathMatchers(HttpMethod.GET, "/products/available").hasAnyRole("BOOTH", "KIOSK")
            .pathMatchers(HttpMethod.GET, "/products/**").hasAnyRole("BOOTH", "KIOSK")
            .pathMatchers(HttpMethod.POST, "/products").hasRole("BOOTH")
            .pathMatchers(HttpMethod.PUT, "/products/**").hasRole("BOOTH")
            .pathMatchers(HttpMethod.PATCH, "/products/{productId}/status").hasRole("BOOTH")
            .pathMatchers(HttpMethod.PATCH, "/products/{productId}/stock").hasRole("BOOTH")
            .pathMatchers(HttpMethod.DELETE, "/products/**").hasRole("BOOTH")

            // 주문 관리
            .pathMatchers(HttpMethod.GET, "/orders").hasAnyRole("BOOTH", "KIOSK")
            .pathMatchers(HttpMethod.GET, "/orders/today").hasAnyRole("BOOTH", "KIOSK")
            .pathMatchers(HttpMethod.GET, "/orders/**").hasAnyRole("BOOTH", "KIOSK")
            .pathMatchers(HttpMethod.POST, "/orders").hasRole("KIOSK")
            .pathMatchers(HttpMethod.POST, "/orders/{orderId}/payment-requests/qr").hasRole("KIOSK")
            .pathMatchers(HttpMethod.POST, "/orders/{orderId}/payment-requests/student-id").hasRole("KIOSK")
            .pathMatchers(HttpMethod.GET, "/orders/{orderId}/payment-request").hasAnyRole("BOOTH", "KIOSK")

            // 부스 정보
            .pathMatchers(HttpMethod.GET, "/booths/me").hasRole("BOOTH")

            .anyExchange().authenticated()
        }
        .build()
}