package com.flick.place.infra.websocket

import com.flick.common.utils.logger
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap

@Component
class PaymentWebSocketHandler : WebSocketHandler {
    private val log = logger()

    private val paymentRequestSessions = ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()
    private val sink = Sinks.many().multicast().onBackpressureBuffer<Pair<Long, String>>()

    init {
        sink.asFlux()
            .subscribe { (requestId, message) ->
                notifyPaymentRequestSessions(requestId, message)
            }
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        val path = session.handshakeInfo.uri.path

        return when {
            path.startsWith("/ws/payment-requests/") -> {
                val requestId = path.substringAfterLast("/").toLongOrNull()

                if (requestId != null) {
                    log.info("WebSocket connected: payment-request/$requestId")

                    paymentRequestSessions
                        .computeIfAbsent(requestId) { mutableSetOf() }
                        .add(session)

                    session.receive()
                        .doFinally {
                            paymentRequestSessions[requestId]?.remove(session)
                            if (paymentRequestSessions[requestId]?.isEmpty() == true) {
                                paymentRequestSessions.remove(requestId)
                            }
                            log.info("WebSocket disconnected: payment-request/$requestId")
                        }
                        .then()
                } else {
                    log.warn("Invalid WebSocket path: $path")
                    session.close().then()
                }
            }
            else -> {
                log.warn("Unsupported WebSocket path: $path")
                session.close().then()
            }
        }
    }

    fun sendPaymentUpdateMessage(requestId: Long, message: String) {
        log.info("Sending update to payment-request/$requestId")
        sink.tryEmitNext(Pair(requestId, message))
    }

    private fun notifyPaymentRequestSessions(requestId: Long, message: String) {
        val sessions = paymentRequestSessions[requestId] ?: return

        if (sessions.isEmpty()) {
            return
        }

        log.info("Broadcasting to ${sessions.size} sessions for payment-request/$requestId")

        sessions.forEach { session ->
            session.send(Mono.just(session.textMessage(message)))
                .subscribe(
                    {},
                    { err -> log.error("Failed to send WebSocket message: ${err.message}") }
                )
        }
    }
}