package com.github.nikitavbv.servicemonitor.project

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.nikitavbv.servicemonitor.PROJECT_API
import com.github.nikitavbv.servicemonitor.agent.Agent
import com.github.nikitavbv.servicemonitor.api.StatusOKResponse
import com.github.nikitavbv.servicemonitor.exceptions.AuthRequiredException
import com.github.nikitavbv.servicemonitor.exceptions.UnknownParameterException
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.BufferedReader
import java.io.Reader
import java.security.InvalidParameterException
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping(PROJECT_API)
class ProjectController(
    val projectRepository: ProjectRepository,

    val memoryMetricRepository: MemoryMetricRepository
) {

    @Autowired
    lateinit var objectMapper: ObjectMapper

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
        return CreateProjectResult(project.id, project.name)
    }

    @PutMapping("/{projectID}")
    fun updateProject(httpRequest: HttpServletRequest, @PathVariable projectID: Long, @RequestBody updates: Map<String, Any>): StatusOKResponse {
        val user = applicationUserRepository.findByUsername(httpRequest.remoteUser)
        val project = user.projects.find { it.id == projectID } ?: throw ProjectNotFoundException()
        updates.keys.forEach {
            when (it) {
                "name" -> project.name = updates[it].toString()
                else -> throw UnknownParameterException(it)
            }
        }
        projectRepository.save(project)
        return StatusOKResponse()
    }

    @DeleteMapping("/{projectID}")
    fun deleteProject(httpRequest: HttpServletRequest, @PathVariable projectID: Long): StatusOKResponse {
        val user = applicationUserRepository.findByUsername(httpRequest.remoteUser)
        val project = user.projects.find { it.id == projectID } ?: throw ProjectNotFoundException()
        projectRepository.delete(project)
        user.projects.remove(project)
        applicationUserRepository.save(user)
        return StatusOKResponse()
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
                    memoryMetricRepository
                )
            ))
        }
        return mapOf(
            "agents" to agentsList
        )
    }
}
