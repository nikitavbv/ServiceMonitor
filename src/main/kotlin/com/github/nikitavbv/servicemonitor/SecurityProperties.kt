package com.github.nikitavbv.servicemonitor

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("app.security")
class SecurityProperties {
    lateinit var secret: String
}
