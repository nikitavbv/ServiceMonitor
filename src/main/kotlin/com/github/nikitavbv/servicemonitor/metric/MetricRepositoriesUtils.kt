package com.github.nikitavbv.servicemonitor.metric

import com.github.nikitavbv.servicemonitor.metric.resources.CPUMetric
import com.github.nikitavbv.servicemonitor.metric.resources.CPUMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.CPUUsageRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DeviceIORepository
import com.github.nikitavbv.servicemonitor.metric.resources.DiskUsageMetric
import com.github.nikitavbv.servicemonitor.metric.resources.DiskUsageMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DockerContainerDataRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DockerMetric
import com.github.nikitavbv.servicemonitor.metric.resources.DockerMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.FilesystemUsageRepository
import com.github.nikitavbv.servicemonitor.metric.resources.IOMetric
import com.github.nikitavbv.servicemonitor.metric.resources.IOMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetric
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MysqlMetric
import com.github.nikitavbv.servicemonitor.metric.resources.MysqlMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkDeviceDataRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkMetric
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NginxMetric
import com.github.nikitavbv.servicemonitor.metric.resources.NginxMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.UptimeMetric
import com.github.nikitavbv.servicemonitor.metric.resources.UptimeMetricRepository

fun saveMemoryMetric(metric: MemoryMetric, metricBase: Metric, memoryMetricRepository: MemoryMetricRepository): Long? {
    metric.metricBase = metricBase
    memoryMetricRepository.save(metric)
    return metric.id
}

fun saveIOMetric(
    metric: IOMetric,
    metricBase: Metric,
    ioMetricRepository: IOMetricRepository,
    deviceIORepository: DeviceIORepository
): Long? {
    metric.metricBase = metricBase
    ioMetricRepository.save(metric)
    metric.devices.forEach { it.metric = metric; deviceIORepository.save(it) }
    return metric.id
}

fun saveDiskUsageMetric(
    metric: DiskUsageMetric,
    metricBase: Metric,
    diskUsageMetricRepository: DiskUsageMetricRepository,
    filesystemUsageRepository: FilesystemUsageRepository
): Long? {
    metric.metricBase = metricBase
    diskUsageMetricRepository.save(metric)
    metric.filesystems.forEach { it.metric = metric; filesystemUsageRepository.save(it) }
    return metric.id
}

fun saveCPUMetric(
    metric: CPUMetric,
    metricBase: Metric,
    cpuMetricRepository: CPUMetricRepository,
    cpuUsageRepository: CPUUsageRepository
): Long? {
    metric.metricBase = metricBase
    cpuMetricRepository.save(metric)
    metric.cpus.forEach { it.metric = metric; cpuUsageRepository.save(it) }
    return metric.id
}

fun saveUptimeMetric(
    metric: UptimeMetric,
    metricBase: Metric,
    uptimeMetricRepository: UptimeMetricRepository
): Long? {
    metric.metricBase = metricBase
    uptimeMetricRepository.save(metric)
    return metric.id
}

fun saveNetworkMetric(
    metric: NetworkMetric,
    metricBase: Metric,
    networkMetricRepository: NetworkMetricRepository,
    networkDeviceDataRepository: NetworkDeviceDataRepository
): Long? {
    metric.metricBase = metricBase
    networkMetricRepository.save(metric)
    metric.devices.forEach { it.metric = metric; networkDeviceDataRepository.save(it) }
    return metric.id
}

fun saveDockerMetric(
    metric: DockerMetric,
    metricBase: Metric,
    dockerMetricRepository: DockerMetricRepository,
    dockerContainerDataRepository: DockerContainerDataRepository
): Long? {
    metric.metricBase = metricBase
    dockerMetricRepository.save(metric)
    metric.containers.forEach { it.metric = metric; dockerContainerDataRepository.save(it) }
    return metric.id
}

fun saveNGINXMetric(
    metric: NginxMetric,
    metricBase: Metric,
    nginxMetricRepository: NginxMetricRepository
): Long? {
    metric.metricBase = metricBase
    nginxMetricRepository.save(metric)
    return metric.id
}

fun saveMySQLMetric(
    metric: MysqlMetric,
    metricBase: Metric,
    mysqlMetricRepository: MysqlMetricRepository
): Long? {
    metric.metricBase = metricBase
    mysqlMetricRepository.save(metric)
    return metric.id
}
