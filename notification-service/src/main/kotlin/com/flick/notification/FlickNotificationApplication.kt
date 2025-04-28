package com.flick.notification

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@SpringBootApplication(
    scanBasePackages = ["com.flick"]
)
class FlickNotificationApplication

fun main(args: Array<String>) {
    runApplication<FlickNotificationApplication>(*args)
}