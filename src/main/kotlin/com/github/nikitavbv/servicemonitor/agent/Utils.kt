package com.github.nikitavbv.servicemonitor.agent

import com.github.nikitavbv.servicemonitor.metric.resources.CPUMetric
import com.github.nikitavbv.servicemonitor.metric.resources.CPUMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DiskUsageMetric
import com.github.nikitavbv.servicemonitor.metric.resources.DiskUsageMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DockerMetric
import com.github.nikitavbv.servicemonitor.metric.resources.DockerMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.IOMetric
import com.github.nikitavbv.servicemonitor.metric.resources.IOMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetric
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MysqlMetric
import com.github.nikitavbv.servicemonitor.metric.resources.MysqlMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkMetric
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NginxMetric
import com.github.nikitavbv.servicemonitor.metric.resources.NginxMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.UptimeMetric
import com.github.nikitavbv.servicemonitor.metric.resources.UptimeMetricRepository

fun getMemoryMetric(memoryMetricRepository: MemoryMetricRepository, id: Long): MemoryMetric {
    return memoryMetricRepository.getOne(id)
}

fun getIOMetric(ioMetricRepository: IOMetricRepository, id: Long): IOMetric {
    return ioMetricRepository.getOne(id)
}

fun getDiskUsageMetric(diskUsageMetricRepository: DiskUsageMetricRepository, id: Long): DiskUsageMetric {
    return diskUsageMetricRepository.getOne(id)
}

fun getCPUMetric(cpuMetricRepository: CPUMetricRepository, id: Long): CPUMetric {
    return cpuMetricRepository.getOne(id)
}

fun getUptimeMetric(uptimeMetricRepository: UptimeMetricRepository, id: Long): UptimeMetric {
    return uptimeMetricRepository.getOne(id)
}

fun getNetworkMetric(networkMetricRepository: NetworkMetricRepository, id: Long): NetworkMetric {
    return networkMetricRepository.getOne(id)
}

fun getDockerMetric(dockerMetricRepository: DockerMetricRepository, id: Long): DockerMetric {
    return dockerMetricRepository.getOne(id)
}

fun getNginxMetric(nginxMetricRepository: NginxMetricRepository, id: Long): NginxMetric {
    return nginxMetricRepository.getOne(id)
}

fun getMysqlMetric(mysqlMetricRepository: MysqlMetricRepository, id: Long): MysqlMetric {
    return mysqlMetricRepository.getOne(id)
}
