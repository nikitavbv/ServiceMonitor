package com.github.nikitavbv.servicemonitor.agent

import com.github.nikitavbv.servicemonitor.AGENT_API
import com.github.nikitavbv.servicemonitor.api.StatusOKResponse
import com.github.nikitavbv.servicemonitor.exceptions.AccessDeniedException
import com.github.nikitavbv.servicemonitor.exceptions.AuthRequiredException
import com.github.nikitavbv.servicemonitor.exceptions.MissingParameterException
import com.github.nikitavbv.servicemonitor.exceptions.UnknownParameterException
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
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.Exception
import java.lang.RuntimeException
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

    @Scheduled(fixedRate = 1000 * 60)
    fun checkEndpoints() {
        agentRepository.findAll()
            .filter {
                it.type == "endpoint"
            }
            .forEach {
                val endpoint = it.properties["endpoint"]

                if (!it.properties.containsKey("totalRequests")) {
                    it.properties["totalRequests"] = "0"
                }
                if (!it.properties.containsKey("totalTime")) {
                    it.properties["totalTime"] = "0"
                }
                if (!it.properties.containsKey("totalErrors")) {
                    it.properties["totalErrors"] = "0"
                }

                val httpClient: CloseableHttpClient? = HttpClients.createMinimal()
                val httpGet = HttpGet(endpoint)
                val startedAt = System.currentTimeMillis()
                var totalTime: Long
                try {
                    val response = httpClient?.execute(httpGet)
                    totalTime = System.currentTimeMillis() - startedAt
                    if (response?.statusLine?.statusCode != HttpStatus.OK.value()) {
                        it.properties["totalErrors"] = ((it.properties["totalErrors"])!!.toLong() + 1).toString()
                    }
                    response?.close()
                } catch (e: Exception) {
                    totalTime = System.currentTimeMillis() - startedAt
                    it.properties["totalErrors"] = ((it.properties["totalErrors"])!!.toLong() + 1).toString()
                }
                it.properties["totalTime"] = ((it.properties["totalTime"])!!.toLong() + totalTime).toString()
                it.properties["totalRequests"] = ((it.properties["totalRequests"])!!.toLong() + 1).toString()
                httpClient?.close()

                agentRepository.save(it)
            }
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    fun resetEndpointStats() {
        agentRepository.findAll()
            .filter { it.type == "endpoint" }
            .forEach {
                it.properties["totalRequests"] = "0"
                it.properties["totalTime"] = "0"
                it.properties["totalErrors"] = "0"
                agentRepository.save(it)
            }
    }

    @GetMapping()
    fun getAgentDetails(httpRequest: HttpServletRequest, token: String): Map<String, Any?> {
        val agent = agentRepository.findByApiKey(token) ?: throw AgentNotFoundException()
        return agent.toMap(
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

    @GetMapping("/{agentID}")
    fun getAgentDetailsByID(req: HttpServletRequest, @PathVariable agentID: Long): Map<String, Any?> {
        val user = applicationUserRepository.findByUsername(req.remoteUser ?: throw AuthRequiredException())
        val agent = agentRepository.findById(agentID).orElseThrow { AgentNotFoundException() }
        val project = agent.project ?: throw RuntimeException("No project set for agent")
        if (!project.users.contains(user)) throw AccessDeniedException()
        return agent.toMap(
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

    @GetMapping("/all")
    fun getAllAgents(httpRequest: HttpServletRequest): Map<String, Any?> {
        val user = applicationUserRepository.findByUsername(httpRequest.remoteUser ?: throw AuthRequiredException())
        val agents: MutableList<Map<String, Any?>> = mutableListOf()
        user.projects.forEach { project ->
            project.agents.forEach { agent ->
                agents.add(
                    agent.toMap(
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

    @PutMapping("/{agentID}")
    fun updateAgent(req: HttpServletRequest, @RequestBody updates: Map<String, Any>, @PathVariable agentID: Long): StatusOKResponse {
        val user = applicationUserRepository.findByUsername(req.remoteUser ?: throw AuthRequiredException())
        val agent = agentRepository.findById(agentID).orElseThrow { AgentNotFoundException() }
        val project = agent.project ?: throw RuntimeException("No project set for agent")
        if (!project.users.contains(user)) throw AccessDeniedException()
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

    @DeleteMapping("/{agentID}")
    fun deleteAgent(req: HttpServletRequest, @PathVariable agentID: Long): StatusOKResponse {
        val user = applicationUserRepository.findByUsername(req.remoteUser ?: throw AuthRequiredException())
        val agent = agentRepository.findById(agentID).orElseThrow { AgentNotFoundException() }
        val project = agent.project ?: throw RuntimeException("No project set for agent")
        if (!project.users.contains(user)) throw AccessDeniedException()
        agentRepository.delete(agent)
        project.agents.remove(agent)
        projectRepository.save(project)
        return StatusOKResponse()
    }
}
