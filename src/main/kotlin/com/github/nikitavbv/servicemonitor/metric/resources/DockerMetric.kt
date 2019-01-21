package com.github.nikitavbv.servicemonitor.metric.resources

import com.github.nikitavbv.servicemonitor.metric.Metric
import com.github.nikitavbv.servicemonitor.metric.MetricType
import java.util.Date
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.OneToOne

@Entity
data class DockerMetric(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    var metricBase: Metric,
    val timestamp: Date,

    @OneToMany(targetEntity = DockerContainerData::class, mappedBy = "metric", fetch = FetchType.EAGER)
    val containers: List<DockerContainerData>
) {

    fun asMap(): Map<String, Any?> {
        return mapOf(
            "type" to MetricType.DOCKER.typeName,
            "id" to metricBase.id,
            "containers" to containers,
            "timestamp" to timestamp
        )
    }
}
