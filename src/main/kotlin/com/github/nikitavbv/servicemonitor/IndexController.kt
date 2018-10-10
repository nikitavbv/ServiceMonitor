package com.github.nikitavbv.servicemonitor

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IndexController {

    @GetMapping("/")
    fun welcome() = "Welcome!"
}
