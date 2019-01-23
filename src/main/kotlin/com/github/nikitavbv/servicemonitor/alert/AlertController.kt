package com.github.nikitavbv.servicemonitor.alert

import com.github.nikitavbv.servicemonitor.ALERT_API
import com.github.nikitavbv.servicemonitor.agent.Agent
import com.github.nikitavbv.servicemonitor.agent.AgentNotFoundException
import com.github.nikitavbv.servicemonitor.agent.AgentRepository
import com.github.nikitavbv.servicemonitor.api.StatusOKResponse
import com.github.nikitavbv.servicemonitor.exceptions.AccessDeniedException
import com.github.nikitavbv.servicemonitor.exceptions.AuthRequiredException
import com.github.nikitavbv.servicemonitor.exceptions.MissingParameterException
import com.github.nikitavbv.servicemonitor.metric.Metric
import com.github.nikitavbv.servicemonitor.metric.MetricNotFoundException
import com.github.nikitavbv.servicemonitor.metric.MetricRepository
import com.github.nikitavbv.servicemonitor.project.Project
import com.github.nikitavbv.servicemonitor.user.ApplicationUser
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping(ALERT_API)
class AlertController(
    val alertRepository: AlertRepository,
    val applicationUserRepository: ApplicationUserRepository,
    val agentRepository: AgentRepository,
    val metricRepository: MetricRepository
) {

    @GetMapping
    fun getAllAlerts(req: HttpServletRequest): Map<String, Any?> {
        val user = applicationUserRepository.findByUsername(req.remoteUser ?: throw AuthRequiredException())
        return mapOf(
            "alerts" to getAlertsForUser(user)
        )
    }

    @PostMapping
    fun createAlert(req: HttpServletRequest, @RequestBody body: Map<String, Any?>): Map<String, Any?> {
        val user = applicationUserRepository.findByUsername(req.remoteUser ?: throw AuthRequiredException())
        val agentID = (body["agentID"] ?: throw MissingParameterException("agentID")).toString().toLong()
        val agent = agentRepository.findById(agentID).orElseThrow { AgentNotFoundException() }
        val project = agent.project ?: throw AssertionError("No project set for agent")
        if (!project.users.contains(user)) throw AccessDeniedException()
        val metricID = (body["metricID"] ?: throw MissingParameterException("metricID")).toString().toLong()
        val metric = metricRepository.findById(metricID).orElseThrow { MetricNotFoundException() }

        val paramName = (body["paramName"] ?: throw MissingParameterException("metricName")).toString()
        val condition = (body["condition"] ?: throw MissingParameterException("condition")).toString()
        val conditionLevel = (body["conditionLevel"] ?: throw MissingParameterException("conditionLevel")).toString()
        val action = (body["alertAction"] ?: throw MissingParameterException("action")).toString()

        val alert = Alert(
            metric = metric,
            paramName = paramName,
            alertCondition = condition,
            level = conditionLevel,
            action = action
        )
        alertRepository.save(alert)
        metric.alerts.add(alert)
        metricRepository.save(metric)

        return mapOf(
            "status" to "ok",
            "id" to alert.id
        )
    }

    @DeleteMapping("/{alertID}")
    fun deleteAlert(req: HttpServletRequest, @PathVariable alertID: Long): StatusOKResponse {
        val user = applicationUserRepository.findByUsername(req.remoteUser ?: throw AuthRequiredException())
        val alert = alertRepository.findById(alertID).orElseThrow { AlertNotFoundException() }
        val agent = alert.metric.agent ?: throw AssertionError("Metric has no agent set")
        val project = agent.project ?: throw AssertionError("Agent has no project set")
        if (!project.users.contains(user)) throw AccessDeniedException()

        alert.metric.alerts.remove(alert)
        alertRepository.delete(alert)
        metricRepository.save(alert.metric)

        return StatusOKResponse()
    }

    fun getAlertsForUser(user: ApplicationUser): List<Map<String, Any?>> {
        val alerts = mutableListOf<Map<String, Any?>>()
        user.projects.forEach {
            alerts.addAll(getAlertsForProject(it))
        }
        return alerts
    }

    fun getAlertsForProject(project: Project): List<Map<String, Any?>> {
        val alerts = mutableListOf<Map<String, Any?>>()
        project.agents.forEach {
            alerts.addAll(getAlertsForAgent(it))
        }
        return alerts
    }

    fun getAlertsForAgent(agent: Agent): List<Map<String, Any?>> {
        val alerts = mutableListOf<Map<String, Any?>>()
        agent.metrics.forEach {
            alerts.addAll(getAlertsForMetric(it))
        }
        return alerts
    }

    fun getAlertsForMetric(metric: Metric): List<Map<String, Any?>> {
        return metric.alerts.map {
            mapOf(
                "alert" to it,
                "metric" to it.metric
            )
        }.toList()
    }
}
