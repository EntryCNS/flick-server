package com.flick.notification.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig {
    companion object {
        private const val TIMEOUT_MS = 5000
        private const val MAX_MEMORY_SIZE = 1024 * 1024 // 1MB
        private const val MAX_CONNECTIONS = 50
        private const val ACQUIRE_TIMEOUT_MS = 10000
        private const val MAX_IDLE_TIME_MS = 60000
    }

    @Bean
    fun expoWebClient(): WebClient {
        val connectionProvider = ConnectionProvider.builder("expo-connection-pool")
            .maxConnections(MAX_CONNECTIONS)
            .pendingAcquireTimeout(Duration.ofMillis(ACQUIRE_TIMEOUT_MS.toLong()))
            .maxIdleTime(Duration.ofMillis(MAX_IDLE_TIME_MS.toLong()))
            .build()

        val httpClient = HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT_MS)
            .responseTimeout(Duration.ofMillis(TIMEOUT_MS.toLong()))
            .doOnConnected { conn: Connection ->
                conn.addHandlerLast(ReadTimeoutHandler(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS))
                conn.addHandlerLast(WriteTimeoutHandler(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS))
            }
            .compress(true)
            .keepAlive(true)
            .wiretap(false)

        val exchangeStrategies = ExchangeStrategies.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(MAX_MEMORY_SIZE)
            }
            .build()

        return WebClient.builder()
            .baseUrl("https://exp.host")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(exchangeStrategies)
            .defaultHeader("Accept", "application/json")
            .defaultHeader("Content-Type", "application/json")
            .build()
    }
}