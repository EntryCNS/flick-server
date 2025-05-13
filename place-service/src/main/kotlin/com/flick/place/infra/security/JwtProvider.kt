package com.flick.place.infra.security

import io.jsonwebtoken.Jwts
import org.apache.hc.core5.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {
    private val boothKey: SecretKey by lazy {
        SecretKeySpec(
            jwtProperties.booth.secret.toByteArray(StandardCharsets.UTF_8),
            Jwts.SIG.HS512.key().build().algorithm
        )
    }

    private val kioskKey: SecretKey by lazy {
        SecretKeySpec(
            jwtProperties.kiosk.secret.toByteArray(StandardCharsets.UTF_8),
            Jwts.SIG.HS512.key().build().algorithm
        )
    }

    fun getType(token: String): JwtType {
        try {
            return JwtType.valueOf(Jwts.parser()
                .verifyWith(boothKey)
                .build()
                .parseSignedClaims(token)
                .header
                .type)
        } catch (e: Exception) {
            return JwtType.valueOf(Jwts.parser()
                .verifyWith(kioskKey)
                .build()
                .parseSignedClaims(token)
                .header
                .type)
        }
    }

    fun getBoothId(token: String): Long {
        try {
            return Jwts.parser()
                .verifyWith(boothKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
                .toLong()
        } catch (e: Exception) {
            return Jwts.parser()
                .verifyWith(kioskKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
                .toLong()
        }
    }

    fun getRole(token: String): String {
        try {
            return Jwts.parser()
                .verifyWith(boothKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .get("role", String::class.java)
        } catch (e: Exception) {
            return Jwts.parser()
                .verifyWith(kioskKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .get("role", String::class.java)
        }
    }

    fun resolveToken(request: ServerHttpRequest) =
        request.headers.getFirst(HttpHeaders.AUTHORIZATION)?.removePrefix("Bearer ")

    fun generateBoothToken(boothId: Long): JwtPayload {
        val now = Date()
        val accessToken = Jwts.builder()
            .header()
            .type(JwtType.ACCESS.name)
            .and()
            .subject(boothId.toString())
            .claim("role", "BOOTH")
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.booth.accessTokenExpiration))
            .signWith(boothKey)
            .compact()
        val refreshToken = Jwts.builder()
            .header()
            .type(JwtType.REFRESH.name)
            .and()
            .subject(boothId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.booth.refreshTokenExpiration))
            .signWith(boothKey)
            .compact()

        return JwtPayload(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    suspend fun generateKioskToken(boothId: Long): JwtPayload {
        val now = Date()
        val accessToken = Jwts.builder()
            .header()
            .type(JwtType.ACCESS.name)
            .and()
            .subject(boothId.toString())
            .claim("role", "KIOSK")
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.kiosk.accessTokenExpiration))
            .signWith(kioskKey)
            .compact()

        return JwtPayload(
            accessToken = accessToken,
        )
    }
}