package com.github.nikitavbv.servicemonitor.metric

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.nikitavbv.servicemonitor.agent.Agent
import com.github.nikitavbv.servicemonitor.alert.Alert
import com.github.nikitavbv.servicemonitor.alert.AlertRepository
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
data class Metric(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var tag: String? = null,
    var type: String? = null,

    var lastEntryID: Long? = null,

    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    val agent: Agent? = null,

    @JsonIgnore
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "metric")
    val alerts: List<Alert> = mutableListOf()
) {


    fun runAlertChecks(metricData: MutableMap<*, *>, alertRepository: AlertRepository) {
        alerts.forEach { it.runCheck(metricData, alertRepository) }
    }

}
