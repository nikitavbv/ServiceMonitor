package com.github.nikitavbv.servicemonitor.project

import com.github.nikitavbv.servicemonitor.PROJECT_API
import com.github.nikitavbv.servicemonitor.exceptions.AuthRequiredException
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping(PROJECT_API)
class ProjectController(
    val projectRepository: ProjectRepository
) {

    @Autowired
    lateinit var applicationUserRepository: ApplicationUserRepository

    @GetMapping()
    fun getProjectList(httpRequest: HttpServletRequest): Map<String, Any> {
        if (httpRequest.remoteUser == null) throw AuthRequiredException()
        val user = applicationUserRepository.findByUsername(httpRequest.remoteUser)
        return mapOf("projects" to user.projects)
    }

    @PostMapping
    fun createProject(@RequestBody project: Project): CreateProjectResult {
        projectRepository.save(project)
        return CreateProjectResult(project.id, project.name)
    }
}
