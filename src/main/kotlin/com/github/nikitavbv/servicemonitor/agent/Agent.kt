package com.github.nikitavbv.servicemonitor.agent

import com.github.nikitavbv.servicemonitor.metric.Metric
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class Agent(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "agent")
    val metrics: List<Metric> = mutableListOf()
)
