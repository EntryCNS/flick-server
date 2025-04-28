package com.flick.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableR2dbcAuditing
@EnableR2dbcRepositories(basePackages = ["com.flick"])
@EnableTransactionManagement
class R2dbcConfig