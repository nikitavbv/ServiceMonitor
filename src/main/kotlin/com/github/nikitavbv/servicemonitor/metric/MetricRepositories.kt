package com.github.nikitavbv.servicemonitor.metric

import com.github.nikitavbv.servicemonitor.metric.resources.CPUMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.CPUUsageRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DeviceIORepository
import com.github.nikitavbv.servicemonitor.metric.resources.DiskUsageMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DockerContainerDataRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DockerMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.FilesystemUsageRepository
import com.github.nikitavbv.servicemonitor.metric.resources.IOMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MysqlMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkDeviceDataRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NginxMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.UptimeMetricRepository
import org.springframework.data.jpa.repository.JpaRepository

class MetricRepositories(
    val memoryMetricRepository: MemoryMetricRepository,
    val ioMetricRepository: IOMetricRepository,
    val diskUsageMetricRepository: DiskUsageMetricRepository,
    val cpuMetricRepository: CPUMetricRepository,
    val uptimeMetricRepository: UptimeMetricRepository,
    val networkMetricRepository: NetworkMetricRepository,
    val dockerMetricRepository: DockerMetricRepository,
    val nginxMetricRepository: NginxMetricRepository,
    val mysqlMetricRepository: MysqlMetricRepository,
    val cpuUsageRepository: CPUUsageRepository? = null,
    val filesystemUsageRepository: FilesystemUsageRepository? = null,
    val dockerContainerDataRepository: DockerContainerDataRepository? = null,
    val deviceIORepository: DeviceIORepository? = null,
    val networkDeviceDataRepository: NetworkDeviceDataRepository? = null
) {

    fun getRepositoryByMetricType(metricType: MetricType): JpaRepository<*, Long> {
        return when (metricType) {
            MetricType.MEMORY -> memoryMetricRepository
            MetricType.IO -> ioMetricRepository
            MetricType.DISK_USAGE -> diskUsageMetricRepository
            MetricType.CPU -> cpuMetricRepository
            MetricType.UPTIME -> uptimeMetricRepository
            MetricType.NETWORK -> networkMetricRepository
            else -> getAppMetricRepositoryByMetricType(metricType)
                ?: throw AssertionError("Unknown metric type: $metricType")
        }
    }

    private fun getAppMetricRepositoryByMetricType(metricType: MetricType): JpaRepository<*, Long>? {
        return when (metricType) {
            MetricType.DOCKER -> dockerMetricRepository
            MetricType.NGINX -> nginxMetricRepository
            MetricType.MYSQL -> mysqlMetricRepository
            else -> null
        }
    }
}
