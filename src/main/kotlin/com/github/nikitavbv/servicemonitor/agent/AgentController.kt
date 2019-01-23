package com.github.nikitavbv.servicemonitor.agent

import com.github.nikitavbv.servicemonitor.AGENT_API
import com.github.nikitavbv.servicemonitor.api.StatusOKResponse
import com.github.nikitavbv.servicemonitor.exceptions.AccessDeniedException
import com.github.nikitavbv.servicemonitor.exceptions.AuthRequiredException
import com.github.nikitavbv.servicemonitor.exceptions.MissingParameterException
import com.github.nikitavbv.servicemonitor.exceptions.UnknownParameterException
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
import com.github.nikitavbv.servicemonitor.project.ProjectNotFoundException
import com.github.nikitavbv.servicemonitor.project.ProjectRepository
import com.github.nikitavbv.servicemonitor.user.ApplicationUser
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
    val dockerMetricRepository: DockerMetricRepository,
    val nginxMetricRepository: NginxMetricRepository,
    val mysqlMetricRepository: MysqlMetricRepository
) {

    @GetMapping()
    fun getAgentDetails(httpRequest: HttpServletRequest, token: String): Map<String, Any?> {
        val agent = agentRepository.findByApiKey(token) ?: throw AgentNotFoundException()
        return agent.toMap(getMetricRepositories())
    }

    @GetMapping("/{agentID}")
    fun getAgentDetailsByID(req: HttpServletRequest, @PathVariable agentID: Long): Map<String, Any?> {
        val user = getApplicationUserByHttpRequest(req)
        val agent = agentRepository.findById(agentID).orElseThrow { AgentNotFoundException() }
        val project = agent.project ?: throw AssertionError("No project set for agent")
        if (!project.users.contains(user)) throw AccessDeniedException()
        return agent.toMap(getMetricRepositories())
    }

    @GetMapping("/all")
    fun getAllAgents(httpRequest: HttpServletRequest): Map<String, Any?> {
        val user = getApplicationUserByHttpRequest(httpRequest)
        val agents: MutableList<Map<String, Any?>> = mutableListOf()
        user.projects.forEach { project ->
            project.agents.forEach { agent ->
                agents.add(
                    agent.toMap(getMetricRepositories())
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
        if (body.containsKey("type")) {
            agent.type = body["type"].toString()
        }
        if (body.containsKey("endpoint")) {
            agent.properties["endpoint"] = body["endpoint"].toString()
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
            when (it) {
                "name" -> agent.name = updates[it].toString()
                "properties" -> updateAgentProperties(agent, updates["properties"] as Map<*, *>)
                "token" -> {}
                else -> throw UnknownParameterException(it)
            }
        }
        agentRepository.save(agent)
        return StatusOKResponse()
    }

    private fun updateAgentProperties(agent: Agent, properties: Map<*, *>) {
        properties.keys.forEach { propertyName ->
            val propertyValue = properties[propertyName].toString()
            agent.properties[propertyName.toString()] = propertyValue
        }
    }

    @PutMapping("/{agentID}")
    fun updateAgent(
        req: HttpServletRequest,
        @RequestBody updates: Map<String, Any>,
        @PathVariable agentID: Long
    ): StatusOKResponse {
        val user = getApplicationUserByHttpRequest(req)
        val agent = agentRepository.findById(agentID).orElseThrow { AgentNotFoundException() }
        val project = agent.project ?: throw AssertionError("No project set for agent")
        if (!project.users.contains(user)) throw AccessDeniedException()
        updates.keys.forEach {
            when (it) {
                "name" -> agent.name = updates[it].toString()
                "tags.add" -> agent.addTags(updates[it].toString().split(","))
                "tags.remove" -> agent.removeTags(updates[it].toString().split(","))
                "properties" -> updateAgentProperties(agent, updates["it"] as Map<*, *>)
                "token" -> {}
                else -> throw UnknownParameterException(it)
            }
        }
        agentRepository.save(agent)
        return StatusOKResponse()
    }

    @DeleteMapping("/{agentID}")
    fun deleteAgent(req: HttpServletRequest, @PathVariable agentID: Long): StatusOKResponse {
        val user = getApplicationUserByHttpRequest(req)
        val agent = agentRepository.findById(agentID).orElseThrow { AgentNotFoundException() }
        val project = agent.project ?: throw AssertionError("No project set for agent")
        if (!project.users.contains(user)) throw AccessDeniedException()
        agentRepository.delete(agent)
        project.agents.remove(agent)
        projectRepository.save(project)
        return StatusOKResponse()
    }

    fun getMetricRepositories(): MetricRepositories {
        return MetricRepositories(
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
    }

    fun getApplicationUserByHttpRequest(req: HttpServletRequest): ApplicationUser {
        return applicationUserRepository.findByUsername(req.remoteUser ?: throw AuthRequiredException())
    }
}
