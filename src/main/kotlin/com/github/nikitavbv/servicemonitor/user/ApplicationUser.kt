package com.github.nikitavbv.servicemonitor.user

import com.github.nikitavbv.servicemonitor.metric.Metric
import com.github.nikitavbv.servicemonitor.project.Project
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotEmpty

@Entity
@Table(name="user")
data class ApplicationUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    val id: Long? = null,

    @NotEmpty(message = "Username is required")
    var username: String,

    @NotEmpty(message = "Password is required")
    var password: String,

    var isAdmin: Boolean = false,

    @ManyToMany(cascade=[CascadeType.ALL])
    @JoinTable(
        name="user_project",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "project_id")])
    var projects: MutableList<Project> = mutableListOf()
)
