package com.flick.domain.inquiry.repository

import com.flick.domain.inquiry.entity.InquiryEntity
import com.flick.domain.inquiry.enums.InquiryCategory
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface InquiryRepository : CoroutineCrudRepository<InquiryEntity, Long> {
}