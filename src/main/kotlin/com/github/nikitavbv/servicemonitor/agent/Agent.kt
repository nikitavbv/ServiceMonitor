package com.github.nikitavbv.servicemonitor.agent

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.nikitavbv.servicemonitor.metric.AbstractMetric
import com.github.nikitavbv.servicemonitor.metric.Metric
import com.github.nikitavbv.servicemonitor.metric.MetricRepositories
import com.github.nikitavbv.servicemonitor.metric.MetricType
import com.github.nikitavbv.servicemonitor.metric.resources.CPUMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DiskUsageMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DockerMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.IOMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MysqlMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NginxMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.UptimeMetricRepository
import com.github.nikitavbv.servicemonitor.project.Project
import java.util.Optional
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.PrePersist
import javax.persistence.Table

@Entity
@Table(name = "agent")
data class Agent(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agent_id")
    var id: Long? = null,

    var name: String? = null,
    var type: String = "generic",

    var apiKey: String? = null,

    @ManyToOne
    @JoinTable(
        name = "project_agent",
        joinColumns = [JoinColumn(name = "agent_id")],
        inverseJoinColumns = [JoinColumn(name = "project_id")]
    )
    @JsonIgnore
    var project: Project? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    val properties: MutableMap<String, String> = mutableMapOf(),

    var tags: String = "",

    @JsonIgnore
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "agent")
    val metrics: List<Metric> = mutableListOf()
) {

    @PrePersist
    private fun generateAPIKey() {
        this.apiKey = UUID.randomUUID().toString()
    }

    fun toMap(metricRepositories: MetricRepositories): Map<String, Any?> {
        var tagsArray = emptyArray<String>()
        if (tags != "") {
            tagsArray = tags.split(",").filter { it != "" }.toTypedArray()
        }

        return mapOf(
            "id" to id,
            "projectID" to project?.id,
            "type" to type,
            "name" to name,
            "properties" to properties,
            "tags" to tagsArray,
            "metrics" to getMetricsAsMap(metricRepositories)
        )
    }

    fun getMetricsAsMap(metricRepositories: MetricRepositories): MutableMap<String, Map<String, Any?>> {
        val metricsData: MutableMap<String, Map<String, Any?>> = mutableMapOf()
        metrics.forEach {
            val tag = it.tag
            val lastEntryId = it.lastEntryID
            if (tag != null && lastEntryId != null) {
                val metricMap = getMetricMap(it, metricRepositories, lastEntryId)
                if (metricMap != null) {
                    metricsData[tag] = metricMap
                }
            }
        }
        return metricsData
    }

    private fun getMetricMap(it: Metric, metricRepositories: MetricRepositories, lastEntryId: Long): Map<String, Any?>? {
        return when (it.type) {
            MetricType.MEMORY.typeName -> {
                metricRepositories.memoryMetricRepository.findById(lastEntryId).orElse(null)
            }
            MetricType.IO.typeName -> {
                metricRepositories.ioMetricRepository.findById(lastEntryId).orElse(null)
            }
            MetricType.DISK_USAGE.typeName -> {
                metricRepositories.diskUsageMetricRepository.findById(lastEntryId).orElse(null)
            }
            MetricType.CPU.typeName -> {
                metricRepositories.cpuMetricRepository.findById(lastEntryId).orElse(null)
            }
            MetricType.UPTIME.typeName -> {
                metricRepositories.uptimeMetricRepository.findById(lastEntryId).orElse(null)
            }
            MetricType.NETWORK.typeName -> {
                metricRepositories.networkMetricRepository.findById(lastEntryId).orElse(null)
            }
            MetricType.DOCKER.typeName -> {
                metricRepositories.dockerMetricRepository.findById(lastEntryId).orElse(null)
            }
            MetricType.NGINX.typeName -> {
                metricRepositories.nginxMetricRepository.findById(lastEntryId).orElse(null)
            }
            MetricType.MYSQL.typeName -> {
                metricRepositories.mysqlMetricRepository.findById(lastEntryId).orElse(null)
            }
            else -> throw RuntimeException("Unknown metric type: $it.type")
        }?.asMap()
    }
}
