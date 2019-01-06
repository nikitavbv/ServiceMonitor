package com.github.nikitavbv.servicemonitor.metric.resources

import org.springframework.data.jpa.repository.JpaRepository

interface DockerMetricRepository : JpaRepository<DockerMetric, Long>
