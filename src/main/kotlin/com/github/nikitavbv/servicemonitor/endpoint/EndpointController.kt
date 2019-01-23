package com.github.nikitavbv.servicemonitor.endpoint

import com.github.nikitavbv.servicemonitor.agent.AgentRepository
import org.apache.http.HttpException
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.RestController

@RestController
class EndpointController(
    var agentRepository: AgentRepository
) {

    @Scheduled(fixedRate = 1000 * 60)
    fun checkEndpoints() {
        agentRepository.findAll()
            .filter {
                it.type == "endpoint"
            }
            .forEach {
                val endpoint = it.properties["endpoint"]

                if (!it.properties.containsKey("totalRequests")) {
                    it.properties["totalRequests"] = "0"
                }
                if (!it.properties.containsKey("totalTime")) {
                    it.properties["totalTime"] = "0"
                }
                if (!it.properties.containsKey("totalErrors")) {
                    it.properties["totalErrors"] = "0"
                }

                val httpClient: CloseableHttpClient? = HttpClients.createMinimal()
                val httpGet = HttpGet(endpoint)
                val startedAt = System.currentTimeMillis()
                var totalTime: Long
                try {
                    val response = httpClient?.execute(httpGet)
                    totalTime = System.currentTimeMillis() - startedAt
                    if (response?.statusLine?.statusCode != HttpStatus.OK.value()) {
                        it.properties["totalErrors"] = ((it.properties["totalErrors"])!!.toLong() + 1).toString()
                    }
                    response?.close()
                } catch (e: HttpException) {
                    totalTime = System.currentTimeMillis() - startedAt
                    it.properties["totalErrors"] = ((it.properties["totalErrors"])!!.toLong() + 1).toString()
                }
                it.properties["totalTime"] = ((it.properties["totalTime"])!!.toLong() + totalTime).toString()
                it.properties["totalRequests"] = ((it.properties["totalRequests"])!!.toLong() + 1).toString()
                httpClient?.close()

                agentRepository.save(it)
            }
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    fun resetEndpointStats() {
        agentRepository.findAll()
            .filter { it.type == "endpoint" }
            .forEach {
                it.properties["totalRequests"] = "0"
                it.properties["totalTime"] = "0"
                it.properties["totalErrors"] = "0"
                agentRepository.save(it)
            }
    }

}
