package com.github.nikitavbv.servicemonitor.agent

import org.springframework.data.jpa.repository.JpaRepository

interface AgentRepository : JpaRepository<Agent, Long> {

    fun findByApiKey(apiKey: String): Agent
}
