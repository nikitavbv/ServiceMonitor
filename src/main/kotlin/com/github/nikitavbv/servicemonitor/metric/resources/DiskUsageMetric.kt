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
data class DiskUsageMetric(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    var metricBase: Metric,
    val timestamp: Date,

    @OneToMany(targetEntity = FilesystemUsage::class, mappedBy = "metric", fetch = FetchType.EAGER)
    val filesystems: List<FilesystemUsage>
) {

    fun asMap(): Map<String, Any?> {
        return mapOf(
            "type" to MetricType.DISK_USAGE.typeName,
            "id" to metricBase.id,
            "filesystems" to filesystems,
            "timestamp" to timestamp
        )
    }

}
