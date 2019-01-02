package com.github.nikitavbv.servicemonitor.project

data class CreateProjectResult(
    val id: Long? = null,
    val name: String? = null,
    val projects: List<Project>? = null
)
