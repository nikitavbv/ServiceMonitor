package com.github.nikitavbv.servicemonitor

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.nikitavbv.servicemonitor.agent.Agent
import com.github.nikitavbv.servicemonitor.agent.AgentRepository
import com.github.nikitavbv.servicemonitor.metric.MetricRepository
import com.github.nikitavbv.servicemonitor.metric.resources.MemoryMetricRepository
import junit.framework.TestCase.assertEquals
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.text.SimpleDateFormat
import java.util.TimeZone

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class MetricTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var agentRepository: AgentRepository

    @Autowired
    lateinit var metricRepository: MetricRepository

    @Autowired
    lateinit var memoryMetricRepository: MemoryMetricRepository

    @Test
    fun `test send metric data`() {
        val metricEntriesBeforeTest = metricRepository.count()
        val memoryMetricEntriesBeforeTest = memoryMetricRepository.count()
        val agent = Agent()
        agentRepository.save(agent)

        val printer = ObjectMapper().writer().withDefaultPrettyPrinter()
        mockMvc.perform(post(METRIC_API)
            .header("Authorization", "API_TOKEN ${agent.apiKey}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(printer.writeValueAsBytes(mapOf(
                "metrics" to arrayOf(mapOf(
                    "tag" to "memory",
                    "type" to "memory",
                    "timestamp" to "2018-10-14T00:31:34",
                    "total" to 8252174336,
                    "free" to 655392768,
                    "available" to 1460305920,
                    "buffers" to 90501120,
                    "cached" to 2492198912,
                    "swapTotal" to 2147479552,
                    "swapFree" to 697143296
                ))
            ))))
            .andExpect(status().isOk)
            .andDo {
                assertEquals(metricEntriesBeforeTest + 1, metricRepository.count())
                assertEquals(memoryMetricEntriesBeforeTest + 1, memoryMetricRepository.count())

                val memoryMetric = memoryMetricRepository.findTopByOrderByIdDesc()
                assertEquals("memory", memoryMetric.metricBase.tag)
                assertEquals("memory", memoryMetric.metricBase.type)
                assertEquals(8252174336, memoryMetric.total)
                assertEquals(655392768, memoryMetric.free)
                assertEquals(1460305920, memoryMetric.available)
                assertEquals(90501120, memoryMetric.buffers)
                assertEquals(2492198912, memoryMetric.cached)
                assertEquals(2147479552, memoryMetric.swapTotal)
                assertEquals(697143296, memoryMetric.swapFree)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                assertEquals(dateFormat.parse("2018-10-14 00:31:34"), memoryMetric.timestamp)

                memoryMetricRepository.delete(memoryMetric)
                agentRepository.delete(agent)
            }
    }

    @Test
    fun `test send invalid metrics array`() {
        val agent = Agent()
        agentRepository.save(agent)

        val printer = ObjectMapper().writer().withDefaultPrettyPrinter()
        mockMvc.perform(post(METRIC_API)
            .header("Authorization", "API_TOKEN ${agent.apiKey}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(printer.writeValueAsBytes(mapOf(
                "metrics" to arrayOf(
                    "just some string",
                    mapOf(
                        "tag" to "memory",
                        "type" to "memory",
                        "timestamp" to "2018-10-14T00:31:34",
                        "total" to 8252174336,
                        "free" to 655392768,
                        "available" to 1460305920,
                        "buffers" to 90501120,
                        "cached" to 2492198912,
                        "swapTotal" to 2147479552,
                        "swapFree" to 697143296
                    )
                )
            ))))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error", equalTo("invalid_parameter_value")))
            .andExpect(jsonPath("$.parameterName", equalTo("metrics")))

        agentRepository.delete(agent)
    }

    @Test
    fun `test send non-string metric tag`() {
        val agent = Agent()
        agentRepository.save(agent)

        val printer = ObjectMapper().writer().withDefaultPrettyPrinter()
        mockMvc.perform(post(METRIC_API)
            .header("Authorization", "API_TOKEN ${agent.apiKey}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(printer.writeValueAsBytes(mapOf(
                "metrics" to arrayOf(
                    mapOf(
                        "tag" to 123456,
                        "type" to "memory",
                        "timestamp" to "2018-10-14T00:31:34",
                        "total" to 8252174336,
                        "free" to 655392768,
                        "available" to 1460305920,
                        "buffers" to 90501120,
                        "cached" to 2492198912,
                        "swapTotal" to 2147479552,
                        "swapFree" to 697143296
                    )
                )
            ))))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error", equalTo("invalid_parameter_value")))
            .andExpect(jsonPath("$.parameterName", equalTo("metrics")))
            .andExpect(jsonPath("$.message", equalTo("non-string metric tag")))
            .andDo {
                agentRepository.delete(agent)
            }
    }

    @Test
    fun `test send non-string metric type`() {
        val agent = Agent()
        agentRepository.save(agent)

        val printer = ObjectMapper().writer().withDefaultPrettyPrinter()
        mockMvc.perform(post(METRIC_API)
            .header("Authorization", "API_TOKEN ${agent.apiKey}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(printer.writeValueAsBytes(mapOf(
                "metrics" to arrayOf(
                    mapOf(
                        "tag" to "memory",
                        "type" to -1337,
                        "timestamp" to "2018-10-14T00:31:34",
                        "total" to 8252174336,
                        "free" to 655392768,
                        "available" to 1460305920,
                        "buffers" to 90501120,
                        "cached" to 2492198912,
                        "swapTotal" to 2147479552,
                        "swapFree" to 697143296
                    )
                )
            ))))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error", equalTo("invalid_parameter_value")))
            .andExpect(jsonPath("$.parameterName", equalTo("metrics")))
            .andExpect(jsonPath("$.message", equalTo("non-string metric type")))

        agentRepository.delete(agent)
    }

    @Test
    fun `test send invalid metrics array elements`() {
        val agent = Agent()
        agentRepository.save(agent)

        val printer = ObjectMapper().writer().withDefaultPrettyPrinter()
        mockMvc.perform(post(METRIC_API)
            .header("Authorization", "API_TOKEN ${agent.apiKey}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(printer.writeValueAsBytes(mapOf(
                "metrics" to "not an array"
            ))))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error", equalTo("invalid_parameter_value")))
            .andExpect(jsonPath("$.parameterName", equalTo("metrics")))
    }

    @Test
    fun `test no api key`() {
        val agent = Agent()
        agentRepository.save(agent)

        val printer = ObjectMapper().writer().withDefaultPrettyPrinter()
        mockMvc.perform(post(METRIC_API)
            .contentType(MediaType.APPLICATION_JSON)
            .content(printer.writeValueAsBytes(mapOf(
                "metrics" to arrayOf(mapOf(
                    "tag" to "memory",
                    "type" to "memory",
                    "timestamp" to "2018-10-14T00:31:34",
                    "total" to 8252174336,
                    "free" to 655392768,
                    "available" to 1460305920,
                    "buffers" to 90501120,
                    "cached" to 2492198912,
                    "swapTotal" to 2147479552,
                    "swapFree" to 697143296
                ))
            ))))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error", equalTo("api_key_required")))

        agentRepository.delete(agent)
    }

    @Test
    fun `test send metric data for non-existing agent`() {
        val printer = ObjectMapper().writer().withDefaultPrettyPrinter()
        mockMvc.perform(post(METRIC_API)
            .header("Authorization", "API_TOKEN 58d629e7-d0b2-4f67-8a43-7b7416025502")
            .contentType(MediaType.APPLICATION_JSON)
            .content(printer.writeValueAsBytes(mapOf(
                "metrics" to arrayOf(mapOf(
                    "tag" to "memory",
                    "type" to "memory",
                    "timestamp" to "2018-10-14T00:31:34",
                    "total" to 8252174336,
                    "free" to 655392768,
                    "available" to 1460305920,
                    "buffers" to 90501120,
                    "cached" to 2492198912,
                    "swapTotal" to 2147479552,
                    "swapFree" to 697143296
                ))
            ))))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error", equalTo("agent_not_found")))
    }

    @Test
    fun `test send non-existing metric type`() {
        val agent = Agent()
        agentRepository.save(agent)

        val printer = ObjectMapper().writer().withDefaultPrettyPrinter()
        mockMvc.perform(post(METRIC_API)
            .header("Authorization", "API_TOKEN ${agent.apiKey}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(printer.writeValueAsBytes(mapOf(
                "metrics" to arrayOf(mapOf(
                    "tag" to "memory",
                    "type" to "wrong_type",
                    "timestamp" to "2018-10-14T00:31:34"
                ))
            ))))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error", equalTo("invalid_parameter_value")))
            .andExpect(jsonPath("$.message", equalTo("unknown metric type: wrong_type")))

        agentRepository.delete(agent)
    }
}
