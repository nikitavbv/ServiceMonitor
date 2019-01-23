package com.github.nikitavbv.servicemonitor.agent

import com.fasterxml.jackson.annotation.JsonIgnore
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
                val memoryMetric = metricRepositories.memoryMetricRepository.findById(lastEntryId)
                if (memoryMetric.isPresent) {
                    memoryMetric.get().asMap()
                }
                null
            }
            MetricType.IO.typeName -> {
                val ioMetric = metricRepositories.ioMetricRepository.findById(lastEntryId)
                if (ioMetric.isPresent) {
                    ioMetric.get().asMap()
                }
                null
            }
            MetricType.DISK_USAGE.typeName -> {
                val diskUsageMetric = metricRepositories.diskUsageMetricRepository.findById(lastEntryId)
                if (diskUsageMetric.isPresent) {
                    diskUsageMetric.get().asMap()
                }
                null
            }
            MetricType.CPU.typeName -> {
                val cpuMetric = metricRepositories.cpuMetricRepository.findById(lastEntryId)
                if (cpuMetric.isPresent) {
                    cpuMetric.get().asMap()
                }
                null
            }
            MetricType.UPTIME.typeName -> {
                val uptimeMetric = metricRepositories.uptimeMetricRepository.findById(lastEntryId)
                if (uptimeMetric.isPresent) {
                    uptimeMetric.get().asMap()
                }
                null
            }
            MetricType.NETWORK.typeName -> {
                val networkMetric = metricRepositories.networkMetricRepository.findById(lastEntryId)
                if (networkMetric.isPresent) {
                    networkMetric.get().asMap()
                }
                null
            }
            MetricType.DOCKER.typeName -> {
                val dockerMetric = metricRepositories.dockerMetricRepository.findById(lastEntryId)
                if (dockerMetric.isPresent) {
                    dockerMetric.get().asMap()
                }
                null
            }
            MetricType.NGINX.typeName -> {
                val nginxMetric = metricRepositories.nginxMetricRepository.findById(lastEntryId)
                if (nginxMetric.isPresent) {
                    nginxMetric.get().asMap()
                }
                null
            }
            MetricType.MYSQL.typeName -> {
                val mysqlMetric = metricRepositories.mysqlMetricRepository.findById(lastEntryId)
                if (mysqlMetric.isPresent) {
                    mysqlMetric.get().asMap()
                }
                null
            }
            else -> throw RuntimeException("Unknown metric type: $it.type")
        }
    }
}
