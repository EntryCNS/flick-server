package com.flick.admin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FlickAdminApplication

fun main(args: Array<String>) {
    runApplication<FlickAdminApplication>(*args)
}