package com.github.nikitavbv.servicemonitor.agent

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.nikitavbv.servicemonitor.metric.Metric
import com.github.nikitavbv.servicemonitor.metric.MetricType
import com.github.nikitavbv.servicemonitor.metric.resources.CPUMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DiskUsageMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DockerMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.IOMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetric
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.UptimeMetricRepository
import com.github.nikitavbv.servicemonitor.project.Project
import org.springframework.beans.factory.annotation.Autowired
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
@Table(name="agent")
data class Agent(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agent_id")
    var id: Long? = null,

    var name: String? = null,

    var apiKey: String? = null,

    @ManyToOne
    @JoinTable(
        name = "project_agent",
        joinColumns = [JoinColumn(name = "agent_id")],
        inverseJoinColumns = [JoinColumn(name = "project_id")]
    )
    @JsonIgnore
    var project: Project? = null,

    @ElementCollection
    val properties: MutableMap<String, String> = mutableMapOf(),

    @JsonIgnore
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "agent")
    val metrics: List<Metric> = mutableListOf()
) {

    @PrePersist
    private fun generateAPIKey() {
        this.apiKey = UUID.randomUUID().toString()
    }

    fun getMetricsAsMap(
        memoryMetricRepository: MemoryMetricRepository,
        ioMetricRepository: IOMetricRepository,
        diskUsageMetricRepository: DiskUsageMetricRepository,
        cpuMetricRepository: CPUMetricRepository,
        uptimeMetricRepository: UptimeMetricRepository,
        networkMetricRepository: NetworkMetricRepository,
        dockerMetricRepository: DockerMetricRepository
    ): MutableMap<String, Map<String, Any?>> {
        val metricsData: MutableMap<String, Map<String, Any?>> = mutableMapOf()
        metrics.forEach {
            val tag = it.tag
            val lastEntryId = it.lastEntryID
            if (tag != null && lastEntryId != null) {
                when (it.type) {
                    MetricType.MEMORY.typeName -> {
                        val memoryMetric = memoryMetricRepository.findById(lastEntryId)
                        if (memoryMetric.isPresent) {
                            metricsData[tag] = memoryMetric.get().asMap()
                        }
                    }
                    MetricType.IO.typeName -> {
                        val ioMetric = ioMetricRepository.findById(lastEntryId)
                        if (ioMetric.isPresent) {
                            metricsData[tag] = ioMetric.get().asMap()
                        }
                    }
                    MetricType.DISK_USAGE.typeName -> {
                        val diskUsageMetric = diskUsageMetricRepository.findById(lastEntryId)
                        if (diskUsageMetric.isPresent) {
                            metricsData[tag] = diskUsageMetric.get().asMap()
                        }
                    }
                    MetricType.CPU.typeName -> {
                        val cpuMetric = cpuMetricRepository.findById(lastEntryId)
                        if (cpuMetric.isPresent) {
                            metricsData[tag] = cpuMetric.get().asMap()
                        }
                    }
                    MetricType.UPTIME.typeName -> {
                        val uptimeMetric = uptimeMetricRepository.findById(lastEntryId)
                        if (uptimeMetric.isPresent) {
                            metricsData[tag] = uptimeMetric.get().asMap()
                        }
                    }
                    MetricType.NETWORK.typeName -> {
                        val networkMetric = networkMetricRepository.findById(lastEntryId)
                        if (networkMetric.isPresent) {
                            metricsData[tag] = networkMetric.get().asMap()
                        }
                    }
                    MetricType.DOCKER.typeName -> {
                        val dockerMetric = dockerMetricRepository.findById(lastEntryId)
                        if (dockerMetric.isPresent) {
                            metricsData[tag] = dockerMetric.get().asMap()
                        }
                    }
                }
            }
        }
        return metricsData
    }
}
