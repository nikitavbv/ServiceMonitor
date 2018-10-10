package com.github.nikitavbv.servicemonitor.user

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.NotEmpty

@Entity
data class ApplicationUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @NotEmpty(message = "Username is required")
    var username: String,

    @NotEmpty(message = "Password is required")
    var password: String,

    var isAdmin: Boolean = false
)
