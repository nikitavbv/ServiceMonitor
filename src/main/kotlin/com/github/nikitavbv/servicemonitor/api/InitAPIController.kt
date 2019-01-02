package com.github.nikitavbv.servicemonitor.api

import com.github.nikitavbv.servicemonitor.INIT_API
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

const val STATUS_SETUP_REQUIRED: String = "setup_required"
const val STATUS_AUTH_REQUIRED: String = "auth_required"
const val STATUS_OK: String = "ok"

@RestController
class InitAPIController(
    val userRepository: ApplicationUserRepository,
    val memoryMetricRepository: MemoryMetricRepository
) {

    @GetMapping(INIT_API)
    fun init(httpRequest: HttpServletRequest): InitAPIResponse {
        return when {
            !checkIfSetupIsDone() -> InitAPIResponse(status = STATUS_SETUP_REQUIRED)
            httpRequest.remoteUser == null -> InitAPIResponse(status = STATUS_AUTH_REQUIRED)
            else -> {
                val user = userRepository.findByUsername(httpRequest.remoteUser)
                val agents: MutableList<Map<String, Any?>> = mutableListOf()
                user.projects.forEach { project ->
                    project.agents.forEach { agent ->
                        agents.add(
                            mapOf(
                                "id" to agent.id,
                                "name" to agent.name,
                                "properties" to agent.properties,
                                "metrics" to agent.getMetricsAsMap(
                                    memoryMetricRepository
                                )
                            )
                        )
                    }
                }
                InitAPIResponse(status = STATUS_OK, agents = agents)
            }
        }
    }

    fun checkIfSetupIsDone() = userRepository.count() > 0
}
