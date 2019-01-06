package com.github.nikitavbv.servicemonitor.metric.resources

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
data class CPUUsage(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    var metric: CPUMetric,

    val cpu: String,
    val user: Double,
    val nice: Double,
    val system: Double,
    val idle: Double,
    val iowait: Double,
    val irq: Double,
    val softirq: Double,
    val guest: Double,
    val steal: Double,
    val guestNice: Double
)
