package com.flick.domain.inquiry.error

import com.flick.common.error.CustomError
import org.springframework.http.HttpStatus

enum class InquiryError(override val status: HttpStatus, override val message: String) : CustomError {
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "Inquiry not found"),
}