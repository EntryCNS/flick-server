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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
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
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*")
        configuration.allowedMethods = listOf("*")
        configuration.allowCredentials = true
        configuration.allowedHeaders = listOf("*")
        configuration.exposedHeaders = listOf("Authorization")
        configuration.allowCredentials = false
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }

        return source
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun configure(http: ServerHttpSecurity, jwtAuthenticationFilter: JwtAuthenticationFilter) = http
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
        .authorizeExchange { it
            .pathMatchers(HttpMethod.GET ,"/swagger-ui/**", "/v3/api-docs/**").permitAll()

            .pathMatchers(HttpMethod.GET, "/ws/**").permitAll()

            .pathMatchers(HttpMethod.POST, "/auth/login").permitAll()
            .pathMatchers(HttpMethod.POST, "/auth/refresh").permitAll()

            .pathMatchers(HttpMethod.GET, "/users/me/balance").hasRole(STUDENT)

            .pathMatchers(HttpMethod.POST, "/payments/requests/confirm").hasRole(STUDENT)

            .anyExchange().authenticated()
        }
        .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .build()
}