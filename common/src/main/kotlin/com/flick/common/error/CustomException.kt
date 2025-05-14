package com.flick.common.error

class CustomException(
    val error: CustomError,
    vararg args: Any?
) : RuntimeException(
    error.message.format(*args)
) {
    val status: Int = error.status.value()
    val code: String = (error as Enum<*>).name
}