package com.github.nikitavbv.servicemonitor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServiceMonitorApplication

fun main(args: Array<String>) {
    runApplication<ServiceMonitorApplication>(args[0])
}
