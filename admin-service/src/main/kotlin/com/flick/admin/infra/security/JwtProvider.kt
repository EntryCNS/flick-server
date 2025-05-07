package com.flick.admin.infra.security

import io.jsonwebtoken.Jwts
import org.apache.hc.core5.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {
    private val key: SecretKey by lazy {
        SecretKeySpec(
            jwtProperties.secret.toByteArray(StandardCharsets.UTF_8),
            Jwts.SIG.HS512.key().build().algorithm
        )
    }

    fun getType(token: String) = JwtType.valueOf(
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .header
            .type
    )

    fun getUserId(token: String) = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .payload
        .subject
        .toLong()

    fun resolveToken(request: ServerHttpRequest) =
        request.headers.getFirst(HttpHeaders.AUTHORIZATION)?.removePrefix("Bearer ")

    suspend fun generateToken(userId: Long): JwtPayload {
        val now = Date()
        val accessToken = Jwts.builder()
            .header()
            .type(JwtType.ACCESS.name)
            .and()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.accessTokenExpiration))
            .signWith(key)
            .compact()
        val refreshToken = Jwts.builder()
            .header()
            .type(JwtType.REFRESH.name)
            .and()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.refreshTokenExpiration))
            .signWith(key)
            .compact()

        return JwtPayload(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}