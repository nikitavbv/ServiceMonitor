package com.github.nikitavbv.servicemonitor

import com.github.nikitavbv.servicemonitor.security.SECRET_SIZE
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.security.SecureRandom
import java.util.Base64

@Configuration
@ConfigurationProperties("app.general")
class ApplicationProperties {
    lateinit var url: String
}
