package com.github.nikitavbv.servicemonitor

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest

@RestController
class IndexController : ErrorController {

    @RequestMapping(value = ["/error"])
    fun error(request: HttpServletRequest): ModelAndView {
        println(request.requestURL)
        return ModelAndView("index.html")
    }

    override fun getErrorPath(): String {
        return "/error"
    }
}
