package com.github.nikitavbv.servicemonitor.api

import com.github.nikitavbv.servicemonitor.INIT_API
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

const val STATUS_SETUP_REQUIRED: String = "setup_required"
const val STATUS_AUTH_REQUIRED: String = "auth_required"
const val STATUS_OK: String = "ok"

@RestController
class InitAPIController(
    val userRepository: ApplicationUserRepository
) {

    @GetMapping(INIT_API)
    fun init(httpRequest: HttpServletRequest): InitAPIResponse {
        return when {
            !checkIfSetupIsDone() -> InitAPIResponse(status = STATUS_SETUP_REQUIRED)
            httpRequest.remoteUser == null -> InitAPIResponse(status = STATUS_AUTH_REQUIRED)
            else -> InitAPIResponse(status = STATUS_OK)
        }
    }

    fun checkIfSetupIsDone() = userRepository.count() > 0
}
