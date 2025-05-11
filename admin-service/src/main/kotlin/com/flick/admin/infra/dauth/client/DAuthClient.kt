package com.flick.admin.infra.dauth.client

import com.flick.admin.infra.dauth.config.DAuthProperties
import com.flick.admin.infra.dauth.dto.response.DAuthCodeResponse
import com.flick.admin.infra.dauth.dto.response.DAuthToken
import com.flick.admin.infra.dauth.dto.response.DAuthUser
import com.flick.admin.infra.dauth.dto.response.DAuthUserResponse
import com.flick.admin.infra.dauth.error.DAuthError
import com.flick.common.error.CustomException
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono

@Component
class DAuthClient(
    private val dAuthProperties: DAuthProperties,
    webClientBuilder: WebClient.Builder
) {
    private val webClient = webClientBuilder
        .codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }
        .build()

    suspend fun login(id: String, password: String): DAuthToken {
        val codeResponse = webClient.post()
            .uri("https://dauth.b1nd.com/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "id" to id,
                    "pw" to password,
                    "clientId" to dAuthProperties.clientId,
                    "redirectUrl" to dAuthProperties.redirectUrl
                )
            )
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) { res ->
                res.bodyToMono(String::class.java)
                    .defaultIfEmpty("DAuth login failed")
                    .flatMap { Mono.error(CustomException(DAuthError.LOGIN_FAILED, it)) }
            }
            .awaitBody<DAuthCodeResponse>()

        val code = codeResponse.data.location.substringAfter("code=").substringBefore("&")

        return webClient.post()
            .uri("https://dauth.b1nd.com/api/token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "code" to code,
                    "client_id" to dAuthProperties.clientId,
                    "client_secret" to dAuthProperties.clientSecret
                )
            )
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) { res ->
                res.bodyToMono(String::class.java)
                    .defaultIfEmpty("DAuth token exchange failed")
                    .flatMap { Mono.error(CustomException(DAuthError.TOKEN_EXCHANGE_FAILED, it)) }
            }
            .awaitBody()
    }

    suspend fun refresh(refreshToken: String): DAuthToken {
        return webClient.post()
            .uri("https://dauth.b1nd.com/api/token/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "refreshToken" to refreshToken,
                    "clientId" to dAuthProperties.clientId
                )
            )
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) { res ->
                res.bodyToMono(String::class.java)
                    .defaultIfEmpty("DAuth token refresh failed")
                    .flatMap { Mono.error(CustomException(DAuthError.REFRESH_FAILED, it)) }
            }
            .awaitBody()
    }

    suspend fun getUser(accessToken: String): DAuthUser {
        return webClient.get()
            .uri("https://opendodam.b1nd.com/api/user")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) { res ->
                res.bodyToMono(String::class.java)
                    .defaultIfEmpty("DAuth user fetch failed")
                    .flatMap { Mono.error(CustomException(DAuthError.USER_FETCH_FAILED, it)) }
            }
            .awaitBody<DAuthUserResponse>()
            .data
    }
}