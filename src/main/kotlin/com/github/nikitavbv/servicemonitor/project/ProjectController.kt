package com.github.nikitavbv.servicemonitor.project

import com.github.nikitavbv.servicemonitor.PROJECT_API
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(PROJECT_API)
class ProjectController(
    val projectRepository: ProjectRepository
) {

    @PostMapping
    fun createProject(@RequestBody project: Project): CreateProjectResult {
        projectRepository.save(project)
        return CreateProjectResult(project.id, project.name)
    }
}