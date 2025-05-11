package com.flick.admin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.flick"])
@ConfigurationPropertiesScan
class FlickAdminApplication

fun main(args: Array<String>) {
    runApplication<FlickAdminApplication>(*args)
}