package com.flick.admin.infra.webflux

import com.flick.admin.domain.booth.BoothStatusConverter
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.reactive.config.WebFluxConfigurationSupport

@Configuration
class WebFluxConfig : WebFluxConfigurationSupport() {
    override fun addFormatters(registry: FormatterRegistry) {
        super.addFormatters(registry)
        registry.addConverter(BoothStatusConverter())
    }
}