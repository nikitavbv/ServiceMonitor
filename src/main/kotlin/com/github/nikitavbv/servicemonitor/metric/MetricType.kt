package com.github.nikitavbv.servicemonitor.metric

import com.github.nikitavbv.servicemonitor.metric.resources.CPUMetric
import com.github.nikitavbv.servicemonitor.metric.resources.DiskUsageMetric
import com.github.nikitavbv.servicemonitor.metric.resources.DockerMetric
import com.github.nikitavbv.servicemonitor.metric.resources.IOMetric
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetric
import com.github.nikitavbv.servicemonitor.metric.resources.MysqlMetric
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkMetric
import com.github.nikitavbv.servicemonitor.metric.resources.NginxMetric
import com.github.nikitavbv.servicemonitor.metric.resources.UptimeMetric
import kotlin.reflect.KClass

enum class MetricType(val typeName: String, val kclass: KClass<*>) {

    MEMORY("memory", MemoryMetric::class),
    IO("io", IOMetric::class),
    DISK_USAGE("diskUsage", DiskUsageMetric::class),
    CPU("cpu", CPUMetric::class),
    UPTIME("uptime", UptimeMetric::class),
    NETWORK("network", NetworkMetric::class),
    DOCKER("docker", DockerMetric::class),
    NGINX("nginx", NginxMetric::class),
    MYSQL("mysql", MysqlMetric::class);

    companion object {
        fun byTypeName(typeName: String): MetricType? {
            return MetricType.values().find { it.typeName == typeName }
        }
    }
}
