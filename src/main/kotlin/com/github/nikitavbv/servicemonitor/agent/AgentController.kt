package com.github.nikitavbv.servicemonitor.agent

import com.github.nikitavbv.servicemonitor.AGENT_API
import com.github.nikitavbv.servicemonitor.exceptions.MissingParameterException
import com.github.nikitavbv.servicemonitor.project.ProjectNotFoundException
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
    fun initAgent(@RequestBody body: Map<String, Any>): Agent {
        val projectAPIKey = (body["token"] ?: throw MissingParameterException("token")).toString()
        val agent = Agent()
        val project = projectRepository.findByApiKey(projectAPIKey) ?: throw ProjectNotFoundException()
        agentRepository.save(agent)
        project.agents.add(agent)
        projectRepository.save(project)
        return agent
    }
}
