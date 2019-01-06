package com.github.nikitavbv.servicemonitor.metric.resources

import org.springframework.data.jpa.repository.JpaRepository

interface NetworkDeviceDataRepository : JpaRepository<NetworkDeviceData, Long>
