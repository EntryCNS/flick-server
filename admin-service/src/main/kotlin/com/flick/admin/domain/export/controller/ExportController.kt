package com.flick.admin.domain.export.controller

import com.flick.admin.domain.export.service.ExportService
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/export")
class ExportController(private val exportService: ExportService) {
    @GetMapping
    suspend fun export(): ResponseEntity<Resource> {
        val resource = exportService.export()

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"festival_results.xlsx\"")
            .body(resource)
    }
}