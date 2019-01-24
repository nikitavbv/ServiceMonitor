package com.github.nikitavbv.servicemonitor.metric

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.nikitavbv.servicemonitor.METRIC_API
import com.github.nikitavbv.servicemonitor.agent.Agent
import com.github.nikitavbv.servicemonitor.agent.AgentNotFoundException
import com.github.nikitavbv.servicemonitor.agent.AgentRepository
import com.github.nikitavbv.servicemonitor.alert.AlertRepository
import com.github.nikitavbv.servicemonitor.api.StatusOKResponse
import com.github.nikitavbv.servicemonitor.exceptions.AccessDeniedException
import com.github.nikitavbv.servicemonitor.exceptions.AuthRequiredException
import com.github.nikitavbv.servicemonitor.exceptions.InvalidParameterValueException
import com.github.nikitavbv.servicemonitor.getRequestAPIToken
import com.github.nikitavbv.servicemonitor.metric.resources.CPUMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.CPUUsageRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DeviceIORepository
import com.github.nikitavbv.servicemonitor.metric.resources.DiskUsageMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DockerContainerDataRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DockerMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.FilesystemUsageRepository
import com.github.nikitavbv.servicemonitor.metric.resources.IOMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MysqlMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkDeviceDataRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NginxMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.UptimeMetricRepository
import com.github.nikitavbv.servicemonitor.user.ApplicationUser
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
import kotlin.reflect.KClass

@RestController
@RequestMapping(METRIC_API)
class MetricController(
    var applicationUserRepository: ApplicationUserRepository,
    var metricRepository: MetricRepository,
    var agentRepository: AgentRepository,

    var memoryMetricRepository: MemoryMetricRepository,
    var ioMetricRepository: IOMetricRepository,
    var deviceIORepository: DeviceIORepository,
    var diskUsageMetricRepository: DiskUsageMetricRepository,
    var filesystemUsageRepository: FilesystemUsageRepository,
    var cpuMetricRepository: CPUMetricRepository,
    var cpuUsageRepository: CPUUsageRepository,
    var uptimeMetricRepository: UptimeMetricRepository,
    var networkMetricRepository: NetworkMetricRepository,
    var networkDeviceDataRepository: NetworkDeviceDataRepository,
    var dockerMetricRepository: DockerMetricRepository,
    var dockerContainerDataRepository: DockerContainerDataRepository,
    var nginxMetricRepository: NginxMetricRepository,
    var mysqlMetricRepository: MysqlMetricRepository,

    var alertRepository: AlertRepository
) {

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

    @GetMapping("/{metricID}")
    fun getData(
        req: HttpServletRequest,
        @PathVariable metricID: Long,
        from: Long,
        to: Long,
        points: Long?
    ): Map<String, Any?> {
        val user = getApplicationUserByHttpRequest(req)
        val metric = metricRepository.findById(metricID).orElseThrow { MetricNotFoundException() }
        val agent = metric.getAgentStrictly()
        val project = agent.project ?: throw AssertionError("No project set for agent")
        if (!project.users.contains(user)) throw AccessDeniedException()
        val sessionFactory = entityManagerFactory.unwrap(SessionFactory::class.java)
        var result: MutableList<Map<String, Any?>>
        val pointsNeeded = points ?: DEFAULT_METRICS_POINTS

        val query = sessionFactory.openSession().createQuery(
            "from ${metric.javaClass.name} m WHERE m.metricBase.id = :id " +
                "AND m.timestamp BETWEEN :fromDate and :toDate"
        )
        query.setParameter("id", metric.id)
        query.setParameter("fromDate", Date(from))
        query.setParameter("toDate", Date(to))
        result = query.stream().map { (it as AbstractMetric).asMap() }.collect(Collectors.toList())

        if (result.size > pointsNeeded) {
            result = reduceDataPoints(result, pointsNeeded)
        }

        return mapOf(
            "metric" to metric,
            "history" to result,
            "alerts" to metric.alerts
        )
    }

    fun reduceDataPoints(points: List<Map<String, Any?>>, pointsNeeded: Long): MutableList<Map<String, Any?>> {
        val scaleFactor = pointsNeeded.toDouble() / points.size
        var currentStep = 0.0
        val result: MutableList<Map<String, Any?>> = mutableListOf()
        points.forEach {
            currentStep += scaleFactor
            if (currentStep >= 1) {
                currentStep -= 1
                result.add(it)
            }
        }
        return result
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

            addMetricRecord(metric, metricType, metricData, mapper)

            metric.runAlertChecks(metricData, alertRepository)
        }

        return StatusOKResponse()
    }

    fun addMetricRecord(metricBase: Metric, metricType: String, metricData: MutableMap<*, *>, mapper: ObjectMapper) {
        val metric = mapMetric(metricType, metricData, mapper)
        metricBase.lastEntryID = metric.saveToRepository(metricBase, MetricRepositories(
            memoryMetricRepository,
            ioMetricRepository,
            diskUsageMetricRepository,
            cpuMetricRepository,
            uptimeMetricRepository,
            networkMetricRepository,
            dockerMetricRepository,
            nginxMetricRepository,
            mysqlMetricRepository,
            cpuUsageRepository,
            filesystemUsageRepository,
            dockerContainerDataRepository,
            deviceIORepository,
            networkDeviceDataRepository
        ))
        metricRepository.save(metricBase)
    }

    fun mapMetric(metricTypeName: String, metricData: MutableMap<*, *>, mapper: ObjectMapper): AbstractMetric {
        val metricType = MetricType.byTypeName(metricTypeName)
            ?: throw InvalidParameterValueException("metric_type", "unknown metric type: wrong_type")
        return mapper.convertValue(metricData, metricType.kclass.java) as AbstractMetric
    }

    fun findAgentByAPIToken(apiToken: String): Agent {
        return agentRepository.findByApiKey(apiToken) ?: throw AgentNotFoundException()
    }

    fun getApplicationUserByHttpRequest(req: HttpServletRequest): ApplicationUser {
        return applicationUserRepository.findByUsername(req.remoteUser ?: throw AuthRequiredException())
    }
}
