package com.flick.admin.infra.webflux

import com.flick.admin.domain.booth.converter.BoothStatusConverter
import com.flick.admin.domain.inquiry.converter.InquiryCategoryConverter
import com.flick.admin.domain.user.converter.UserRoleTypeConverter
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.reactive.config.WebFluxConfigurationSupport

@Configuration
class WebFluxConfig : WebFluxConfigurationSupport() {
    override fun addFormatters(registry: FormatterRegistry) {
        super.addFormatters(registry)
        registry.addConverter(BoothStatusConverter())
        registry.addConverter(UserRoleTypeConverter())
        registry.addConverter(InquiryCategoryConverter())
    }
}