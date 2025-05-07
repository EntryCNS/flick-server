package com.flick.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication(
    scanBasePackages = [
        "com.flick"
    ]
)
@ConfigurationPropertiesScan
@EnableDiscoveryClient
class FlickCoreApplication

fun main(args: Array<String>) {
    runApplication<FlickCoreApplication>(*args)
}