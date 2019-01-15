package com.github.nikitavbv.servicemonitor

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("app.general")
class ApplicationProperties {
    lateinit var url: String
}
