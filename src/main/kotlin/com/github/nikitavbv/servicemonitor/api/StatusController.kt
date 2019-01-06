package com.github.nikitavbv.servicemonitor.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController(STATUS_OK)
class StatusController {

    @GetMapping
    fun statusOK(): StatusOKResponse {
        return StatusOKResponse()
    }

}
