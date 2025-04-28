package com.flick.serviceregistry

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@SpringBootApplication
@EnableEurekaServer
class FlickServiceRegistryApplication

fun main(args: Array<String>) {
    runApplication<FlickServiceRegistryApplication>(*args)
}