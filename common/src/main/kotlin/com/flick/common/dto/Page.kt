package com.flick.common.dto


data class Page<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
) {
    companion object {
        fun <T> of(
            content: List<T>,
            pageNumber: Int,
            pageSize: Int,
            totalElements: Long
        ): Page<T> {
            val totalPages = if (pageSize > 0)
                (totalElements + pageSize - 1) / pageSize
            else 0

            val first = pageNumber == 1
            val last = pageNumber >= totalPages
            val empty = content.isEmpty()

            return Page(
                content = content,
                pageNumber = pageNumber,
                pageSize = pageSize,
                totalElements = totalElements,
                totalPages = totalPages.toInt(),
                first = first,
                last = last,
                empty = empty
            )
        }
    }
}