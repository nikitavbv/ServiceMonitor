package com.github.nikitavbv.servicemonitor.project

import com.github.nikitavbv.servicemonitor.PROJECT_API
import com.github.nikitavbv.servicemonitor.api.StatusOKResponse
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
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import org.springframework.beans.factory.annotation.Autowired
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
@RequestMapping(PROJECT_API)
class ProjectController(
    val projectRepository: ProjectRepository,

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

    @Autowired
    lateinit var applicationUserRepository: ApplicationUserRepository

    @GetMapping()
    fun getProjectList(httpRequest: HttpServletRequest): Map<String, Any> {
        val user = applicationUserRepository.findByUsername(httpRequest.remoteUser)
        return mapOf("projects" to user.projects)
    }

    @PostMapping
    fun createProject(httpRequest: HttpServletRequest, @RequestBody project: Project): CreateProjectResult {
        val user = applicationUserRepository.findByUsername(httpRequest.remoteUser)
        projectRepository.save(project)
        user.projects.add(project)
        applicationUserRepository.save(user)
        return CreateProjectResult(project.id, project.name, user.projects)
    }

    @GetMapping("/{projectID}")
    fun getProject(httpRequest: HttpServletRequest, @PathVariable projectID: Long): Map<String, Any?> {
        val user = applicationUserRepository.findByUsername(httpRequest.remoteUser)
        val project = user.projects.find { it.id == projectID } ?: throw ProjectNotFoundException()
        val metrics: MutableList<Map<String, Any?>> = mutableListOf()
        val agents: MutableList<Map<String, Any?>> = mutableListOf()

        project.agents.forEach { agent ->
            agents.add(mapOf(
                "name" to agent.name,
                "properties" to agent.properties
            ))

            val metricsMap = agent.getMetricsAsMap(
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
            metricsMap.values.forEach {
                val metricMap: MutableMap<String, Any?> = mutableMapOf()
                metricMap.putAll(it)
                metricMap["agent"] = agent.id
                metrics.add(metricMap)
            }
        }

        return mapOf(
            "starredMetrics" to project.starredMetrics,
            "metrics" to metrics,
            "agents" to agents
        )
    }

    @PutMapping("/{projectID}")
    fun updateProject(httpRequest: HttpServletRequest, @PathVariable projectID: Long, @RequestBody updates: Map<String, Any>): StatusOKResponse {
        val user = applicationUserRepository.findByUsername(httpRequest.remoteUser)
        val project = user.projects.find { it.id == projectID } ?: throw ProjectNotFoundException()
        updates.keys.forEach {
            when (it) {
                "name" -> project.name = updates[it].toString()
                "user.add" -> {
                    val userToAdd = applicationUserRepository.findByUsername(updates[it].toString())
                    project.users.add(userToAdd)
                    userToAdd.projects.add(project)
                    applicationUserRepository.save(userToAdd)
                }
                else -> throw UnknownParameterException(it)
            }
        }
        projectRepository.save(project)
        return StatusOKResponse()
    }

    @DeleteMapping("/{projectID}")
    fun deleteProject(httpRequest: HttpServletRequest, @PathVariable projectID: Long): Map<String, Any> {
        val user = applicationUserRepository.findByUsername(httpRequest.remoteUser)
        val project = user.projects.find { it.id == projectID } ?: throw ProjectNotFoundException()
        projectRepository.delete(project)
        user.projects.remove(project)
        applicationUserRepository.save(user)
        return mapOf(
            "projects" to user.projects
        )
    }

    @GetMapping("/{projectID}/agents")
    fun getProjectAgents(httpRequest: HttpServletRequest, @PathVariable projectID: Long): Map<String, Any> {
        val user = applicationUserRepository.findByUsername(httpRequest.remoteUser)
        val project = user.projects.find { it.id == projectID } ?: throw ProjectNotFoundException()
        val agentsList: MutableList<Map<String, Any?>> = mutableListOf()
        project.agents.forEach { agent ->
            agentsList.add(mapOf(
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
                    dockerMetricRepository,
                    nginxMetricRepository,
                    mysqlMetricRepository
                )
            ))
        }
        return mapOf(
            "agents" to agentsList
        )
    }

    @PutMapping("/{projectID}/starMetric")
    fun starMetric(httpRequest: HttpServletRequest, @PathVariable projectID: Long, @RequestBody body: Map<String, Any?>): StatusOKResponse {
        val user = applicationUserRepository.findByUsername(httpRequest.remoteUser)
        val project = user.projects.find { it.id == projectID } ?: throw ProjectNotFoundException()
        val metricID = (body["metricID"] ?: throw MissingParameterException("metricID")).toString().toLong()
        if (!project.starredMetrics.contains(metricID)) {
            project.starredMetrics.add(metricID)
        }
        projectRepository.save(project)
        return StatusOKResponse()
    }

    @PutMapping("/{projectID}/unstarMetric")
    fun unstarMetric(httpRequest: HttpServletRequest, @PathVariable projectID: Long, @RequestBody body: Map<String, Any?>): StatusOKResponse {
        val user = applicationUserRepository.findByUsername(httpRequest.remoteUser)
        val project = user.projects.find { it.id == projectID } ?: throw ProjectNotFoundException()
        val metricID = (body["metricID"] ?: throw MissingParameterException("metricID")).toString().toLong()
        project.starredMetrics.remove(metricID)
        projectRepository.save(project)
        return StatusOKResponse()
    }
}
