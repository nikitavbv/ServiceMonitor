package com.github.nikitavbv.servicemonitor.metric.resources

import com.github.nikitavbv.servicemonitor.metric.Metric
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
)
