package com.github.nikitavbv.servicemonitor.project

import com.github.nikitavbv.servicemonitor.agent.Agent
import com.github.nikitavbv.servicemonitor.user.ApplicationUser
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.JoinTable
import javax.persistence.JoinColumn
import javax.persistence.PrePersist

@Entity
data class Project(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String?,

    var apiKey: String? = null,

    @ManyToMany
    @JoinTable(
        name = "project_users",
        joinColumns = [JoinColumn(name = "project_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")])
    var users: List<ApplicationUser> = mutableListOf(),

    @ManyToMany
    @JoinTable(
        name = "project_agents",
        joinColumns = [JoinColumn(name = "project_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "agent_id", referencedColumnName = "id")]
    )
    var agents: MutableList<Agent> = mutableListOf()

) {

    @PrePersist
    fun generateKey() {
        if (this.apiKey != null) {
            return
        }
        this.apiKey = UUID.randomUUID().toString()
    }
}
