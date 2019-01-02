package com.github.nikitavbv.servicemonitor.alert

import org.springframework.data.jpa.repository.JpaRepository

interface AlertRepository : JpaRepository<Alert, Long>
