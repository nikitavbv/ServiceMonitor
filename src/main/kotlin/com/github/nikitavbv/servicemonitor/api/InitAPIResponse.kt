package com.github.nikitavbv.servicemonitor.api

data class InitAPIResponse(
    val status: String,
    val agents: MutableList<Map<String, Any?>>? = null
)
