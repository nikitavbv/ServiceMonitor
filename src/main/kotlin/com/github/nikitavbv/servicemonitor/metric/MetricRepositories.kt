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

data class MetricRepositories(
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
)
