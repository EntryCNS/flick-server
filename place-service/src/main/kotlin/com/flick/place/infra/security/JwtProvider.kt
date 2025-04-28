package com.flick.place.infra.security

import io.jsonwebtoken.Jwts
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.spec.SecretKeySpec


@Component
class JwtProvider(
    private val jwtProperties: JwtProperties
) {
    private val key = SecretKeySpec(
        jwtProperties.secret.toByteArray(StandardCharsets.UTF_8),
        Jwts.SIG.HS256.key().build().algorithm
    )

    fun generateBoothToken(boothId: Long): String {
        val now = Date()
        return Jwts.builder()
            .claim("type", JwtType.BOOTH.name)
            .subject(boothId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.expiration))
            .signWith(key)
            .compact()
    }

    fun generateKioskToken(boothId: Long, sessionId: Long): String {
        val now = Date()
        return Jwts.builder()
            .claim("type", JwtType.KIOSK.name)
            .subject(boothId.toString())
            .claim("sessionId", sessionId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.expiration))
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getPayload(token: String): JwtPayload {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        val type = JwtType.valueOf(claims.get("type", String::class.java))
        val id = claims.subject.toLong()
        val sessionId = if (type == JwtType.KIOSK) {
            claims.get("sessionId", String::class.java).toLong()
        } else null

        return JwtPayload(id, type, sessionId)
    }

    fun extractToken(request: ServerHttpRequest): String? {
        return request.headers.getFirst(HttpHeaders.AUTHORIZATION)?.let {
            if (it.startsWith("Bearer ")) it.substring(7) else null
        }
    }
}