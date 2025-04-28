package com.flick.common.error

import org.springframework.http.HttpStatus

interface CustomError {
    val status: HttpStatus
    val message: String
}