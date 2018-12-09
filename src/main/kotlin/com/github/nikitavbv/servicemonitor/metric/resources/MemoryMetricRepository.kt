package com.github.nikitavbv.servicemonitor.metric.resources

import org.springframework.data.jpa.repository.JpaRepository

interface MemoryMetricRepository : JpaRepository<MemoryMetric, Long> {

    fun findTopByOrderByIdDesc(): MemoryMetric

}
