package com.github.nikitavbv.servicemonitor.project

import com.github.nikitavbv.servicemonitor.agent.Agent
import com.github.nikitavbv.servicemonitor.user.ApplicationUser
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.JoinTable
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.PrePersist
import javax.persistence.Table

@Entity
@Table(name = "project")
data class Project(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    var id: Long? = null,

    var name: String?,

    var apiKey: String? = null,

    @ManyToMany(mappedBy="projects")
    var users: List<ApplicationUser> = mutableListOf(),

    @OneToMany
    @JoinTable(
        name = "project_agent",
        joinColumns = [JoinColumn(name = "project_id")],
        inverseJoinColumns = [JoinColumn(name = "agent_id")]
    )
    var agents: MutableList<Agent> = mutableListOf()

) {

    @PrePersist
    private fun generateKey() {
        this.apiKey = UUID.randomUUID().toString()
    }
}
