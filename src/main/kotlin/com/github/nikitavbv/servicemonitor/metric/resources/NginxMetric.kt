package com.github.nikitavbv.servicemonitor.metric.resources

import com.github.nikitavbv.servicemonitor.metric.Metric
import com.github.nikitavbv.servicemonitor.metric.MetricType
import java.util.Date
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
data class NginxMetric (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    var metricBase: Metric,
    val timestamp: Date,

    val requests: Double
) {

    fun asMap(): Map<String, Any?> {
        return mapOf(
            "type" to MetricType.NGINX.typeName,
            "id" to metricBase.id,
            "requests" to requests,
            "timestamp" to timestamp
        )
    }

}
