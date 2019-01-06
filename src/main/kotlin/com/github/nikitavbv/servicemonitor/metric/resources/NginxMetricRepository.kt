package com.github.nikitavbv.servicemonitor.metric.resources

import org.springframework.data.jpa.repository.JpaRepository

interface NginxMetricRepository : JpaRepository<NginxMetric, Long>
