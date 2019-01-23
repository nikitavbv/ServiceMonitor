package com.github.nikitavbv.servicemonitor.api

import com.github.nikitavbv.servicemonitor.INIT_API
import com.github.nikitavbv.servicemonitor.metric.MetricRepositories
import com.github.nikitavbv.servicemonitor.metric.resources.CPUMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DiskUsageMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DockerMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.IOMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MysqlMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NginxMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.UptimeMetricRepository
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
    val memoryMetricRepository: MemoryMetricRepository,
    val ioMetricRepository: IOMetricRepository,
    val diskUsageMetricRepository: DiskUsageMetricRepository,
    val cpuMetricRepository: CPUMetricRepository,
    val uptimeMetricRepository: UptimeMetricRepository,
    val networkMetricRepository: NetworkMetricRepository,
    val dockerMetricRepository: DockerMetricRepository,
    val nginxMetricRepository: NginxMetricRepository,
    val mysqlMetricRepository: MysqlMetricRepository
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
                            agent.toMap(
                                MetricRepositories(
                                    memoryMetricRepository,
                                    ioMetricRepository,
                                    diskUsageMetricRepository,
                                    cpuMetricRepository,
                                    uptimeMetricRepository,
                                    networkMetricRepository,
                                    dockerMetricRepository,
                                    nginxMetricRepository,
                                    mysqlMetricRepository
                                 )
                            )
                        )
                    }
                }
                InitAPIResponse(status = STATUS_OK, agents = agents, projects = user.projects)
            }
        }
    }

    fun checkIfSetupIsDone() = userRepository.count() > 0
}
