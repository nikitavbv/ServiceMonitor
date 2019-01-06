package com.github.nikitavbv.servicemonitor.agent

import com.github.nikitavbv.servicemonitor.AGENT_API
import com.github.nikitavbv.servicemonitor.api.StatusOKResponse
import com.github.nikitavbv.servicemonitor.exceptions.AuthRequiredException
import com.github.nikitavbv.servicemonitor.exceptions.MissingParameterException
import com.github.nikitavbv.servicemonitor.exceptions.UnknownParameterException
import com.github.nikitavbv.servicemonitor.metric.resources.CPUMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DiskUsageMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DockerMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.IOMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.UptimeMetricRepository
import com.github.nikitavbv.servicemonitor.project.ProjectNotFoundException
import com.github.nikitavbv.servicemonitor.project.ProjectRepository
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping(AGENT_API)
class AgentController(
    var agentRepository: AgentRepository,
    var projectRepository: ProjectRepository,
    var applicationUserRepository: ApplicationUserRepository,

    val memoryMetricRepository: MemoryMetricRepository,
    val ioMetricRepository: IOMetricRepository,
    val diskUsageMetricRepository: DiskUsageMetricRepository,
    val cpuMetricRepository: CPUMetricRepository,
    val uptimeMetricRepository: UptimeMetricRepository,
    val networkMetricRepository: NetworkMetricRepository,
    val dockerMetricRepository: DockerMetricRepository
) {

    @GetMapping()
    fun getAgentDetails(token: String): Map<String, Any?> {
        val agent = agentRepository.findByApiKey(token) ?: throw AgentNotFoundException()
        return mapOf(
            "id" to agent.id,
            "name" to agent.name,
            "properties" to agent.properties,
            "metrics" to agent.getMetricsAsMap(
                memoryMetricRepository,
                ioMetricRepository,
                diskUsageMetricRepository,
                cpuMetricRepository,
                uptimeMetricRepository,
                networkMetricRepository,
                dockerMetricRepository
            )
        )
    }

    @GetMapping("/all")
    fun getAllAgents(httpRequest: HttpServletRequest): Map<String, Any?> {
        val user = applicationUserRepository.findByUsername(httpRequest.remoteUser ?: throw AuthRequiredException())
        val agents: MutableList<Map<String, Any?>> = mutableListOf()
        user.projects.forEach { project ->
            project.agents.forEach { agent ->
                agents.add(
                    mapOf(
                        "id" to agent.id,
                        "name" to agent.name,
                        "properties" to agent.properties,
                        "metrics" to agent.getMetricsAsMap(
                            memoryMetricRepository,
                            ioMetricRepository,
                            diskUsageMetricRepository,
                            cpuMetricRepository,
                            uptimeMetricRepository,
                            networkMetricRepository,
                            dockerMetricRepository
                        )
                    )
                )
            }
        }
        return mapOf(
            "agents" to agents
        )
    }

    @PostMapping
    fun initAgent(@RequestBody body: Map<String, Any>): Agent {
        val projectAPIKey = (body["token"] ?: throw MissingParameterException("token")).toString()
        val agent = Agent()
        if (body.containsKey("name")) {
            agent.name = body["name"].toString()
        }
        val project = projectRepository.findByApiKey(projectAPIKey) ?: throw ProjectNotFoundException()
        agentRepository.save(agent)
        project.agents.add(agent)
        projectRepository.save(project)
        return agent
    }

    @PutMapping
    fun updateAgent(@RequestBody updates: Map<String, Any>): StatusOKResponse {
        val agentAPIKey = (updates["token"] ?: throw MissingParameterException("token")).toString()
        val agent = agentRepository.findByApiKey(agentAPIKey) ?: throw AgentNotFoundException()
        updates.keys.forEach {
            if (it != "token") {
                when (it) {
                    "name" -> agent.name = updates[it].toString()
                    "properties" -> {
                        val properties = updates["properties"] as Map<*, *>
                        properties.keys.forEach { propertyName ->
                            val propertyValue = properties[propertyName].toString()
                            agent.properties[propertyName.toString()] = propertyValue
                        }
                    }
                    else -> throw UnknownParameterException(it)
                }
            }
        }
        agentRepository.save(agent)
        return StatusOKResponse()
    }

    @DeleteMapping()
    fun deleteAgent(@RequestBody body: Map<String, Any>): StatusOKResponse {
        val agentAPIKey = (body["token"] ?: throw MissingParameterException("token")).toString()
        val agent = agentRepository.findByApiKey(agentAPIKey) ?: throw AgentNotFoundException()
        val project = agent.project
        agentRepository.delete(agent)
        if (project != null) {
            project.agents.remove(agent)
            projectRepository.save(project)
        }
        return StatusOKResponse()
    }
}
