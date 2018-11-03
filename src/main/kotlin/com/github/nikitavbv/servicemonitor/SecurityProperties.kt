package com.github.nikitavbv.servicemonitor

import com.github.nikitavbv.servicemonitor.security.SECRET_SIZE
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.security.SecureRandom
import java.util.Base64

@Configuration
@ConfigurationProperties("app.security")
class SecurityProperties {
    lateinit var secret: String

    fun generateSecret(): String {
        val random = SecureRandom()
        val randomBytes = ByteArray(SECRET_SIZE)
        random.nextBytes(randomBytes)
        return Base64.getEncoder().encodeToString(randomBytes)
    }
}
