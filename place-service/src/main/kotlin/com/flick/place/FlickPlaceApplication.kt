package com.flick.place

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@SpringBootApplication(
    scanBasePackages = ["com.flick"]
)
@ConfigurationPropertiesScan
class FlickPlaceApplication

fun main(args: Array<String>) {
    runApplication<FlickPlaceApplication>(*args)
}