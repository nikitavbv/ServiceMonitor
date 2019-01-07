package com.github.nikitavbv.servicemonitor.api

import com.github.nikitavbv.servicemonitor.STATUS_API
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController(STATUS_API)
class StatusController {

    @GetMapping
    fun statusOK(): StatusOKResponse {
        return StatusOKResponse()
    }

}
