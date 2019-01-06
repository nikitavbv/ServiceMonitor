package com.github.nikitavbv.servicemonitor.api

import com.github.nikitavbv.servicemonitor.project.Project

data class InitAPIResponse(
    val status: String,
    val projects: List<Project>? = null,
    val agents: MutableList<Map<String, Any?>>? = null
)
