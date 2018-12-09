package com.github.nikitavbv.servicemonitor.metric

import com.github.nikitavbv.servicemonitor.agent.Agent
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
data class Metric(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var lastEntryID: Long? = null,

    var tag: String? = null,
    var type: String? = null,

    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    val agent: Agent? = null
)
