package com.github.nikitavbv.servicemonitor

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest

const val ERROR_STATUS_CODE_ATTRIBUTE = "javax.servlet.error.status_code"
const val ERROR_MESSAGE_ATTRIBUTE = "javax.servlet.error.message"

@RestController
class IndexController : ErrorController {

    @RequestMapping(value = ["/error"])
    fun error(req: HttpServletRequest): Any {
        val errorStatusCode = req.getAttribute(ERROR_STATUS_CODE_ATTRIBUTE) as Int

        if (errorStatusCode == HttpStatus.NOT_FOUND.value()) {
            return ModelAndView("index.html")
        }

        val errorMessage = req.getAttribute(ERROR_MESSAGE_ATTRIBUTE)
        return ResponseEntity.status(errorStatusCode).body(mapOf(
            "error" to errorMessage
        ))
    }

    override fun getErrorPath(): String {
        return "/error"
    }
}
