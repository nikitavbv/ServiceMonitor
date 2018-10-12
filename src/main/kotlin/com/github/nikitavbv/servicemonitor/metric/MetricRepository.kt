package com.github.nikitavbv.servicemonitor.metric

import org.springframework.data.jpa.repository.JpaRepository

interface MetricRepository : JpaRepository<Metric, Long>
