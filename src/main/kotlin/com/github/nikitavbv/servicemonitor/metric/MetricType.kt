package com.github.nikitavbv.servicemonitor.metric

enum class MetricType(val typeName: String) {

    MEMORY("memory"),
    IO("io"),
    DISK_USAGE("diskUsage"),
    CPU("cpu"),
    UPTIME("uptime"),
    NETWORK("network"),
    DOCKER("docker"),
    NGINX("nginx"),
    MYSQL("mysql")
}
