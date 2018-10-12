package com.github.nikitavbv.servicemonitor.project

import com.github.nikitavbv.servicemonitor.user.ApplicationUser
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.JoinTable
import javax.persistence.JoinColumn

@Entity
data class Project(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String?,

    @ManyToMany
    @JoinTable(
        name = "project_users",
        joinColumns = [JoinColumn(name = "project_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")])
    var users: List<ApplicationUser> = mutableListOf()
)
