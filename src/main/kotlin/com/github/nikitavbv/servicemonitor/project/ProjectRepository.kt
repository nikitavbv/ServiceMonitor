package com.github.nikitavbv.servicemonitor.project

import org.springframework.data.jpa.repository.JpaRepository

interface ProjectRepository : JpaRepository<Project, Long> {

    fun findByApiKey(apiKey: String): Project?
}
