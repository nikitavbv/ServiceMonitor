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
import com.github.nikitavbv.servicemonitor.exceptions.MissingAPIKeyException
import com.github.nikitavbv.servicemonitor.exceptions.MissingParameterException
import com.github.nikitavbv.servicemonitor.metric.resources.CPUMetric
import com.github.nikitavbv.servicemonitor.metric.resources.CPUMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.CPUUsageRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DeviceIORepository
import com.github.nikitavbv.servicemonitor.metric.resources.DiskUsageMetric
import com.github.nikitavbv.servicemonitor.metric.resources.DiskUsageMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DockerContainerDataRepository
import com.github.nikitavbv.servicemonitor.metric.resources.DockerMetric
import com.github.nikitavbv.servicemonitor.metric.resources.DockerMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.FilesystemUsageRepository
import com.github.nikitavbv.servicemonitor.metric.resources.IOMetric
import com.github.nikitavbv.servicemonitor.metric.resources.IOMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetric
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MysqlMetric
import com.github.nikitavbv.servicemonitor.metric.resources.MysqlMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkDeviceDataRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkMetric
import com.github.nikitavbv.servicemonitor.metric.resources.NetworkMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.NginxMetric
import com.github.nikitavbv.servicemonitor.metric.resources.NginxMetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.UptimeMetric
import com.github.nikitavbv.servicemonitor.metric.resources.UptimeMetricRepository
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
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
    fun getData(req: HttpServletRequest, @PathVariable metricID: Long, from: Long, to: Long, points: Long?): Map<String, Any?> {
        val user = applicationUserRepository.findByUsername(req.remoteUser ?: throw AuthRequiredException())
        val metric = metricRepository.findById(metricID).orElseThrow { MetricNotFoundException() }
        val agent = metric.agent ?: throw RuntimeException("No agent set for metric")
        val project = agent.project ?: throw RuntimeException("No project set for agent")
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

    fun addMetricRecord(metric: Metric, metricType: String, metricData: MutableMap<*, *>, mapper: ObjectMapper) {
        when (metricType) {
            MetricType.MEMORY.typeName -> addMemoryRecord(
                metric,
                mapper.convertValue(metricData, MemoryMetric::class.java)
            )
            MetricType.IO.typeName -> addIORecord(
                metric,
                mapper.convertValue(metricData, IOMetric::class.java)
            )
            MetricType.DISK_USAGE.typeName -> addDiskUsageRecord(
                metric,
                mapper.convertValue(metricData, DiskUsageMetric::class.java)
            )
            MetricType.CPU.typeName -> addCPURecord(
                metric,
                mapper.convertValue(metricData, CPUMetric::class.java)
            )
            MetricType.UPTIME.typeName -> addUptimeRecord(
                metric,
                mapper.convertValue(metricData, UptimeMetric::class.java)
            )
            MetricType.NETWORK.typeName -> addNetworkRecord(
                metric,
                mapper.convertValue(metricData, NetworkMetric::class.java)
            )
            MetricType.DOCKER.typeName -> addDockerRecord(
                metric,
                mapper.convertValue(metricData, DockerMetric::class.java)
            )
            MetricType.NGINX.typeName -> addNginxRecord(
                metric,
                mapper.convertValue(metricData, NginxMetric::class.java)
            )
            MetricType.MYSQL.typeName -> addMysqlRecord(
                metric,
                mapper.convertValue(metricData, MysqlMetric::class.java)
            )
            else -> throw InvalidParameterValueException(METRICS_BODY_KEY, "unknown metric type: $metricType")
        }
    }

    fun addMemoryRecord(metricBase: Metric, metric: MemoryMetric) {
        metric.metricBase = metricBase
        memoryMetricRepository.save(metric)
        metricBase.lastEntryID = metric.id
        metricRepository.save(metricBase)
    }

    fun addIORecord(metricBase: Metric, metric: IOMetric) {
        metric.metricBase = metricBase
        ioMetricRepository.save(metric)
        metric.devices.forEach { it.metric = metric; deviceIORepository.save(it) }
        metricBase.lastEntryID = metric.id
        metricRepository.save(metricBase)
    }

    fun addDiskUsageRecord(metricBase: Metric, metric: DiskUsageMetric) {
        metric.metricBase = metricBase
        diskUsageMetricRepository.save(metric)
        metric.filesystems.forEach { it.metric = metric; filesystemUsageRepository.save(it) }
        metricBase.lastEntryID = metric.id
        metricRepository.save(metricBase)
    }

    fun addCPURecord(metricBase: Metric, metric: CPUMetric) {
        metric.metricBase = metricBase
        cpuMetricRepository.save(metric)
        metric.cpus.forEach { it.metric = metric; cpuUsageRepository.save(it) }
        metricBase.lastEntryID = metric.id
        metricRepository.save(metricBase)
    }

    fun addUptimeRecord(metricBase: Metric, metric: UptimeMetric) {
        metric.metricBase = metricBase
        uptimeMetricRepository.save(metric)
        metricBase.lastEntryID = metric.id
        metricRepository.save(metricBase)
    }

    fun addNetworkRecord(metricBase: Metric, metric: NetworkMetric) {
        metric.metricBase = metricBase
        networkMetricRepository.save(metric)
        metric.devices.forEach { it.metric = metric; networkDeviceDataRepository.save(it) }
        metricBase.lastEntryID = metric.id
        metricRepository.save(metricBase)
    }

    fun addDockerRecord(metricBase: Metric, metric: DockerMetric) {
        metric.metricBase = metricBase
        dockerMetricRepository.save(metric)
        metric.containers.forEach { it.metric = metric; dockerContainerDataRepository.save(it) }
        metricBase.lastEntryID = metric.id
        metricRepository.save(metricBase)
    }

    fun addNginxRecord(metricBase: Metric, metric: NginxMetric) {
        metric.metricBase = metricBase
        nginxMetricRepository.save(metric)
        metricBase.lastEntryID = metric.id
        metricRepository.save(metricBase)
    }

    fun addMysqlRecord(metricBase: Metric, metric: MysqlMetric) {
        metric.metricBase = metricBase
        mysqlMetricRepository.save(metric)
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
