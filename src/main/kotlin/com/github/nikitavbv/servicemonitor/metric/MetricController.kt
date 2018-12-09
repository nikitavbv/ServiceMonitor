package com.github.nikitavbv.servicemonitor.metric

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.nikitavbv.servicemonitor.METRIC_API
import com.github.nikitavbv.servicemonitor.agent.Agent
import com.github.nikitavbv.servicemonitor.agent.AgentNotFoundException
import com.github.nikitavbv.servicemonitor.agent.AgentRepository
import com.github.nikitavbv.servicemonitor.api.StatusOKResponse
import com.github.nikitavbv.servicemonitor.exceptions.AccessDeniedException
import com.github.nikitavbv.servicemonitor.exceptions.AuthRequiredException
import com.github.nikitavbv.servicemonitor.exceptions.InvalidParameterValueException
import com.github.nikitavbv.servicemonitor.exceptions.MissingAPIKeyException
import com.github.nikitavbv.servicemonitor.exceptions.MissingParameterException
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetric
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Date
import java.util.stream.Collectors
import javax.persistence.EntityManagerFactory
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping(METRIC_API)
class MetricController(
    var applicationUserRepository: ApplicationUserRepository,
    var metricRepository: MetricRepository,
    var agentRepository: AgentRepository,

    var memoryMetricRepository: MemoryMetricRepository
) {

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

    @GetMapping("/{metricID}")
    fun getData(req: HttpServletRequest, @PathVariable metricID: Long, from: Long, to: Long, points: Long?): Map<String, Any?> {
        val user = applicationUserRepository.findByUsername(req.remoteUser ?: throw AuthRequiredException())
        val metric = metricRepository.findById(metricID).orElseThrow { MetricNotFoundException() }
        val agent = metric.agent ?: throw RuntimeException("No agent set for metric")
        val project = agent.project ?: throw RuntimeException("No project set for agent")
        if (!project.users.contains(user)) throw AccessDeniedException()
        val sessionFactory = entityManagerFactory.unwrap(SessionFactory::class.java)
        var result: MutableList<Map<String, Any?>>
        val pointsNeeded = points ?: DEFAULT_METRICS_POINTS
        when(metric.type) {
            "memory" -> {
                val query = sessionFactory.openSession().createQuery("from MemoryMetric m WHERE m.metricBase.id = :id AND m.timestamp BETWEEN :fromDate AND :toDate")
                query.setParameter("id", metric.id)
                query.setParameter("fromDate", Date(from))
                query.setParameter("toDate", Date(to))
                result = query.stream().map { (it as MemoryMetric).asMap() }.collect(Collectors.toList())
            }
            else -> throw throw InvalidParameterValueException(METRICS_BODY_KEY, "unknown metric type: ${metric.type}")
        }

        if (result.size > pointsNeeded) {
            val scaleFactor = pointsNeeded.toDouble() / result.size
            var currentStep: Double = 0.0
            val newResult: MutableList<Map<String, Any?>> = mutableListOf()
            result.forEach {
                currentStep += scaleFactor
                if (currentStep >= 1) {
                    currentStep -= 1
                    newResult.add(it)
                }
            }
            result = newResult
        }

        return mapOf(
            "data" to result
        )
    }

    @PostMapping
    fun addData(req: HttpServletRequest, @RequestBody body: Map<String, Any>): StatusOKResponse {
        val agent = findAgentByAPIToken(getRequestAPIToken(body))
        val metrics = getMapList(body, METRICS_BODY_KEY)

        val mapper = ObjectMapper()
        metrics.forEach { metricsElement ->
            val metricData = anyToMutableMap(metricsElement, METRICS_BODY_KEY)
            val metricTag = getMetricStringField(metricData, METRICS_BODY_KEY, METRIC_FIELD_TAG)
            val metricType = getMetricStringField(metricData, METRICS_BODY_KEY, METRIC_FIELD_TYPE)
            arrayOf(METRIC_FIELD_TAG, METRIC_FIELD_TYPE).forEach { metricData.remove(it) }

            var metric = agent.metrics.find { it.tag == metricTag }
            if (metric == null) {
                metric = Metric(tag = metricTag, type = metricType, agent = agent)
                metricRepository.save(metric)
            }

            when (metricType) {
                MetricType.MEMORY.typeName -> addMemoryRecord(
                    metric,
                    mapper.convertValue(metricData, MemoryMetric::class.java)
                )
                else -> throw InvalidParameterValueException(METRICS_BODY_KEY, "unknown metric type: $metricType")
            }
        }

        return StatusOKResponse()
    }

    fun addMemoryRecord(metricBase: Metric, metric: MemoryMetric) {
        metric.metricBase = metricBase
        memoryMetricRepository.save(metric)
        metricBase.lastEntryID = metric.id
        metricRepository.save(metricBase)
    }

    fun getRequestAPIToken(body: Map<String, Any>): String {
        return (body["token"] ?: throw MissingAPIKeyException()).toString()
    }

    fun findAgentByAPIToken(apiToken: String): Agent {
        return agentRepository.findByApiKey(apiToken) ?: throw AgentNotFoundException()
    }

    fun getMapList(m: Map<String, Any>, parameter: String): List<*> {
        return (m[parameter] ?: throw MissingParameterException(parameter)) as? List<*>
            ?: throw InvalidParameterValueException(parameter)
    }

    fun anyToMutableMap(a: Any?, parameterName: String): MutableMap<*, *> {
        return a as? MutableMap<*, *> ?: throw InvalidParameterValueException(parameterName)
    }

    fun getMetricStringField(metricData: MutableMap<*, *>, metricsParameterName: String, fieldName: String): String {
        return metricData[fieldName] as? String
            ?: throw InvalidParameterValueException(metricsParameterName, "non-string metric $fieldName")
    }
}
