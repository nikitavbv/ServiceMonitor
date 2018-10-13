package com.github.nikitavbv.servicemonitor.agent

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.nikitavbv.servicemonitor.metric.Metric
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.PrePersist

@Entity
data class Agent(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    var description: String?,
    var apiKey: String?,

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "agent")
    val metrics: List<Metric> = mutableListOf()
) {

    @PrePersist
    fun generateAPIKey() {
        this.apiKey = UUID.randomUUID().toString()
    }
}
