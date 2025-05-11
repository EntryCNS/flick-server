package com.flick.admin.infra.websocket

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
    private val boothWebSocketHandler: BoothWebSocketHandler
) {
    @Bean
    fun webSocketHandlerMapping(): HandlerMapping {
        val urlMap = mapOf(
            "/ws/booth-ranking" to boothWebSocketHandler
        )

        val handlerMapping = SimpleUrlHandlerMapping()
        handlerMapping.urlMap = urlMap
        handlerMapping.order = -1

        return handlerMapping
    }

    @Bean
    fun webSocketService(): WebSocketService = HandshakeWebSocketService(ReactorNettyRequestUpgradeStrategy())

    @Bean
    fun handlerAdapter(webSocketService: WebSocketService): WebSocketHandlerAdapter = WebSocketHandlerAdapter(webSocketService)
}