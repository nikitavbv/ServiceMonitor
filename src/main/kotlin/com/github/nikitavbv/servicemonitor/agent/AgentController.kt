package com.github.nikitavbv.servicemonitor.agent

import com.github.nikitavbv.servicemonitor.AGENT_API
import com.github.nikitavbv.servicemonitor.exceptions.MissingParameterException
import com.github.nikitavbv.servicemonitor.project.ProjectRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AGENT_API)
class AgentController(
    var agentRepository: AgentRepository,
    var projectRepository: ProjectRepository
) {

    @PostMapping
    fun initAgent(@RequestBody agent: Agent, @RequestBody body: Map<String, Any>): Agent {
        val projectAPIKey = (body["projectKey"] ?: throw MissingParameterException("projectKey")).toString()

        agentRepository.save(agent)
        val project = projectRepository.findByApiKey(projectAPIKey)
        // project.agents.add(agent)
        projectRepository.save(project)
        return agent
    }
}
