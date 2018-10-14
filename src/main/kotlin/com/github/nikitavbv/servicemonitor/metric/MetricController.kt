package com.github.nikitavbv.servicemonitor.metric

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.nikitavbv.servicemonitor.METRIC_API
import com.github.nikitavbv.servicemonitor.agent.Agent
import com.github.nikitavbv.servicemonitor.agent.AgentNotFoundException
import com.github.nikitavbv.servicemonitor.agent.AgentRepository
import com.github.nikitavbv.servicemonitor.exceptions.InvalidParameterValueException
import com.github.nikitavbv.servicemonitor.exceptions.MissingAPIKeyException
import com.github.nikitavbv.servicemonitor.exceptions.MissingParameterException
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetric
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping(METRIC_API)
class MetricController(
    var metricRepository: MetricRepository,
    var agentRepository: AgentRepository,

    var memoryMetricRepository: MemoryMetricRepository
) {

    @PostMapping
    fun addData(req: HttpServletRequest, @RequestBody body: Map<String, Any>) {
        val agent = findAgentByAPIToken(getRequestAPIToken(req))
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
    }

    fun addMemoryRecord(metricBase: Metric, metric: MemoryMetric) {
        metric.metricBase = metricBase
        memoryMetricRepository.save(metric)
    }

    fun getRequestAPIToken(req: HttpServletRequest): String {
        return (req.getHeader(API_KEY_HEADER) ?: throw MissingAPIKeyException()).removePrefix(API_KEY_PREFIX)
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
