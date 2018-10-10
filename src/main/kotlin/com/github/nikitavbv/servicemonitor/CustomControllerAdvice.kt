package com.github.nikitavbv.servicemonitor

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.github.nikitavbv.servicemonitor.security.PermissionDeniedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class CustomControllerAdvice {

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        exception: HttpMessageNotReadableException
    ): ResponseEntity<Map<String, Any>> {
        if (exception.cause!!::class == MissingKotlinParameterException::class) {
            val missingParameterName = (exception.cause as MissingKotlinParameterException).parameter.name!!
            return ResponseEntity.badRequest().body(mapOf(
                    "error" to "missing parameter",
                    "missing_parameter" to missingParameterName
            ))
        }
        return ResponseEntity.badRequest().body(mapOf("error" to "failed to parse request"))
    }

    @ExceptionHandler(PermissionDeniedException::class)
    fun handlePermissionDeniedException(exception: PermissionDeniedException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("message" to exception.message))
    }
}
