package com.github.nikitavbv.servicemonitor.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class StatusOKResponse : ResponseEntity<Map<String, Any>>(
    mapOf("status" to "ok"), HttpStatus.OK
)
