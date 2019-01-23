package com.github.nikitavbv.servicemonitor

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.github.nikitavbv.servicemonitor.exceptions.AuthRequiredException
import com.github.nikitavbv.servicemonitor.exceptions.InvalidParameterValueException
import com.github.nikitavbv.servicemonitor.exceptions.MissingAPIKeyException
import com.github.nikitavbv.servicemonitor.exceptions.MissingParameterException
import com.github.nikitavbv.servicemonitor.exceptions.UnknownParameterException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.lang.IllegalArgumentException

@ControllerAdvice
class CustomControllerAdvice {

    @ExceptionHandler(AuthRequiredException::class)
    fun handleAuthRequiredException(
        exception: AuthRequiredException
    ): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf(
            "error" to "auth_required"
        ))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        exception: HttpMessageNotReadableException
    ): ResponseEntity<Map<String, Any>> {
        if (exception.cause != null && exception.cause!!::class == MissingKotlinParameterException::class) {
            val missingParameterName = (exception.cause as MissingKotlinParameterException).parameter.name!!
            return ResponseEntity.badRequest().body(mapOf(
                    "error" to "missing_parameter",
                    "missing_parameter" to missingParameterName
            ))
        }
        return ResponseEntity.badRequest().body(mapOf("error" to "failed to parse request"))
    }

    @ExceptionHandler(MissingParameterException::class)
    fun handleMissingParameterException(
        exception: MissingParameterException
    ): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.badRequest().body(mapOf(
            "error" to "missing_parameter",
            "missing_parameter" to exception.parameterName
        ))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        exception: IllegalArgumentException
    ): ResponseEntity<Map<String, Any?>> {
        val exceptionMessage = exception.message ?: return ResponseEntity.badRequest().body(mapOf(
            "error" to "illegal_parameter"
        ))

        if (exceptionMessage.startsWith("Parameter specified as non-null is null: ")) {
            val parameterName = exceptionMessage.substring(
                exceptionMessage.indexOf(", parameter ") + ", parameter ".length
            )
            return ResponseEntity.badRequest().body(mapOf(
                "error" to "missing_parameter",
                "missing_parameter" to parameterName
            ))
        }

        return ResponseEntity.badRequest().body(mapOf(
            "error" to "illegal_parameter"
        ))
    }

    @ExceptionHandler(MissingAPIKeyException::class)
    fun handleMissingAPIKeyException(exception: MissingAPIKeyException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.badRequest().body(mapOf(
            "error" to "api_key_required"
        ))
    }

    @ExceptionHandler(UnknownParameterException::class)
    fun handleUnknownParameterException(
        exception: UnknownParameterException
    ): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.badRequest().body(mapOf(
            "error" to "unknown_parameter",
            "parameterName" to exception.parameterName
        ))
    }

    @ExceptionHandler(InvalidParameterValueException::class)
    fun handleInvalidParameterValueException(
        exception: InvalidParameterValueException
    ): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.badRequest().body(if (exception.parameterMessage != null)
            mapOf(
                "error" to "invalid_parameter_value",
                "parameterName" to exception.parameterName,
                "message" to exception.parameterMessage
            )
        else
            mapOf(
                "error" to "invalid_parameter_value",
                "parameterName" to exception.parameterName
            )
        )
    }
}
