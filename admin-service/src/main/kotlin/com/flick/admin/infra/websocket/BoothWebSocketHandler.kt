package com.flick.admin.infra.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.flick.admin.domain.booth.dto.response.BoothRankingResponse
import com.flick.common.utils.logger
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap

@Component
class BoothWebSocketHandler(
    private val objectMapper: ObjectMapper,
) : WebSocketHandler {
    private val log = logger()
    private val sessions = ConcurrentHashMap.newKeySet<WebSocketSession>()
    private val sink = Sinks.many().multicast().onBackpressureBuffer<BoothRankingResponse>()

    init {
        sink.asFlux()
            .subscribe { ranking ->
                notifyAllSessions(ranking)
            }
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        val path = session.handshakeInfo.uri.path

        return if (path == "/ws/booth-ranking") {
            log.info("WebSocket connected: /ws/booth-ranking")
            sessions.add(session)

            session.receive()
                .doFinally {
                    sessions.remove(session)
                    log.info("WebSocket disconnected: /ws/booth-ranking")
                }
                .then()
        } else {
            log.warn("Unsupported WebSocket path: $path")
            session.close().then()
        }
    }

    fun sendRankingUpdate(ranking: BoothRankingResponse) {
        log.info("Sending booth ranking update to ${sessions.size} sessions")
        sink.tryEmitNext(ranking)
    }

    private fun notifyAllSessions(ranking: BoothRankingResponse) {
        val message = objectMapper.writeValueAsString(ranking)

        Flux.fromIterable(sessions)
            .flatMap(
                { session ->
                    session.send(Mono.just(session.textMessage(message)))
                        .doOnError { e -> log.error("Failed to send ranking: ${e.message}") }
                        .onErrorResume { Mono.empty() }
                },
                8
            )
            .subscribe()
    }
}