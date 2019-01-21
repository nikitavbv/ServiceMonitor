package com.github.nikitavbv.servicemonitor

import com.github.nikitavbv.servicemonitor.agent.AgentNotFoundException
import com.github.nikitavbv.servicemonitor.exceptions.UserNotFoundException
import com.github.nikitavbv.servicemonitor.metric.MetricNotFoundException
import com.github.nikitavbv.servicemonitor.project.ProjectNotFoundException
import io.jsonwebtoken.security.SignatureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class NotFoundExceptionControllerAdvice {

    @ExceptionHandler(AgentNotFoundException::class)
    fun handleAgentNotFoundException(exception: AgentNotFoundException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "agent_not_found"))
    }

    @ExceptionHandler(ProjectNotFoundException::class)
    fun handleProjectNotFoundException(exception: ProjectNotFoundException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "project_not_found"))
    }

    @ExceptionHandler(MetricNotFoundException::class)
    fun handleMetricNotFoundException(exception: MetricNotFoundException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "metric_not_found"))
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(exception: UserNotFoundException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "user_not_found"))
    }

    @ExceptionHandler(SignatureException::class)
    fun handleSignatureException(exception: SignatureException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to "auth_failed"))
    }
}
