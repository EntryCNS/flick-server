package com.flick.apigateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FlickApiGatewayApplication

fun main(args: Array<String>) {
    runApplication<FlickApiGatewayApplication>(*args)
}