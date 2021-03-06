package com.github.nikitavbv.servicemonitor.metric.resources

import com.github.nikitavbv.servicemonitor.metric.AbstractMetric
import com.github.nikitavbv.servicemonitor.metric.Metric
import com.github.nikitavbv.servicemonitor.metric.MetricRepositories
import com.github.nikitavbv.servicemonitor.metric.MetricType
import java.util.Date
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
data class MemoryMetric(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    var metricBase: Metric,
    val timestamp: Date,

    val total: Long,
    val free: Long,
    val available: Long,
    val buffers: Long,
    val cached: Long,
    val swapTotal: Long,
    val swapFree: Long
) : AbstractMetric() {

    override fun saveToRepository(metricBase: Metric, metricRepositories: MetricRepositories): Long? {
        this.metricBase = metricBase
        metricRepositories.memoryMetricRepository.save(this)
        return id
    }

    override fun asMap(): Map<String, Any?> {
        return mapOf(
            "type" to MetricType.MEMORY.typeName,
            "id" to metricBase.id,
            "total" to total,
            "free" to free,
            "available" to available,
            "buffers" to buffers,
            "cached" to cached,
            "swapTotal" to swapTotal,
            "swapFree" to swapFree,
            "timestamp" to timestamp
        )
    }
}
