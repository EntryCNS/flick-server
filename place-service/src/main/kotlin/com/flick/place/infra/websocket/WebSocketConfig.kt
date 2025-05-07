package com.flick.place.infra.websocket

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.WebSocketService
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy

@Configuration
class WebSocketConfig(
    private val paymentWebSocketHandler: PaymentWebSocketHandler
) {
    @Bean
    fun webSocketHandlerMapping(): HandlerMapping {
        val urlMap = mapOf(
            "/ws/payment-requests/**" to paymentWebSocketHandler
        )

        val handlerMapping = SimpleUrlHandlerMapping()
        handlerMapping.urlMap = urlMap
        handlerMapping.order = -1

        return handlerMapping
    }

    @Bean
    fun webSocketService(): WebSocketService {
        return HandshakeWebSocketService(ReactorNettyRequestUpgradeStrategy())
    }

    @Bean
    fun handlerAdapter(webSocketService: WebSocketService): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter(webSocketService)
    }
}