package com.github.nikitavbv.ServiceMonitor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServiceMonitorApplication

fun main(args: Array<String>) {
    runApplication<ServiceMonitorApplication>(*args)
}
