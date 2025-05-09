package com.flick.admin.infra.dauth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

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
            .awaitBody()
    }

    suspend fun getUser(accessToken: String): DAuthUser {
        return webClient.get()
            .uri("https://opendodam.b1nd.com/api/user")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody<DAuthUserResponse>()
            .data
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class DAuthCodeResponse(
    val data: CodeLocation
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CodeLocation(
    val location: String
)

data class DAuthToken(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("refresh_token") val refreshToken: String? = null,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("expires_in") val expiresIn: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DAuthUserResponse(
    val data: DAuthUser
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DAuthUser(
    val uniqueId: String,
    val grade: Int?,
    val room: Int?,
    val number: Int?,
    val name: String,
    val profileImage: String,
    val role: String,
    val email: String
)