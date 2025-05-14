package com.flick.common.error

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono

@Component
@Order(-2)
class GlobalErrorWebExceptionHandler(private val objectMapper: ObjectMapper) : ErrorWebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> = mono {
        val response = exchange.response
        val path = exchange.request.uri.path

        val errorResponse = createErrorResponse(ex, path)

        response.statusCode = HttpStatusCode.valueOf(errorResponse.status)
        response.headers.contentType = MediaType.APPLICATION_JSON

        val dataBuffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(errorResponse))
        response.writeWith(Mono.just(dataBuffer)).awaitSingleOrNull()
    }

    private fun createErrorResponse(ex: Throwable, path: String): ErrorResponse {
        return when (ex) {
            is CustomException -> ErrorResponse(
                status = ex.status,
                code = ex.code,
                message = ex.message ?: "알 수 없는 오류",
                path = path
            )

            is ResponseStatusException -> ErrorResponse(
                status = ex.statusCode.value(),
                code = ex.statusCode.toString(),
                message = ex.reason ?: "알 수 없는 오류",
                path = path
            )

            is ServerWebInputException -> ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                code = "INVALID_INPUT",
                message = ex.reason ?: "잘못된 입력값",
                path = path
            )

            is IllegalArgumentException -> ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                code = "INVALID_ENUM",
                message = ex.message ?: "잘못된 enum",
                path = path
            )

            else -> ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                code = "INTERNAL_SERVER_ERROR",
                message = ex.message ?: "서버 내부 오류",
                path = path
            ).also {
                ex.printStackTrace()
            }
        }
    }
}