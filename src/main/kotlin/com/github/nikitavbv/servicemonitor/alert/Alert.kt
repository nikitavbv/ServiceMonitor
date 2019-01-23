package com.github.nikitavbv.servicemonitor.alert

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.nikitavbv.servicemonitor.metric.Metric
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.util.Date
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

private const val ALERT_MIN_TIME_DIFFERENCE = 5 * 60 * 1000L

@Entity
data class Alert(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    val paramName: String,
    val alertCondition: String,
    val level: String,
    val action: String,

    var triggered: Boolean = false,
    var triggeredAt: Date? = null,
    var actionExecuted: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id")
    @JsonIgnore
    val metric: Metric
) {

    fun runCheck(data: MutableMap<*, *>, alertRepository: AlertRepository) {
        val state = checkIfTrigger(data) ?: return

        if (state && !actionExecuted) {
            checkTimeAndRunAction(state, alertRepository)
            return
        }

        if (state == triggered) {
            return
        }

        if (state) {
            triggeredAt = Date()
            actionExecuted = false
        }

        triggered = state
        alertRepository.save(this)
    }

    private fun checkTimeAndRunAction(state: Boolean, alertRepository: AlertRepository) {
        val triggerDate = triggeredAt
        if (triggerDate != null) {
            val timeDifference = Date().time - triggerDate.time
            if (timeDifference > ALERT_MIN_TIME_DIFFERENCE) {
                runAction(action)
                actionExecuted = true
                triggered = state
                alertRepository.save(this)
            } else {
                return
            }
        } else {
            triggeredAt = Date()
            alertRepository.save(this)
            return
        }
    }

    private fun runAction(action: String) {
        val httpClient = HttpClients.createDefault()
        val httpGet = HttpGet(action)
        val res = httpClient.execute(httpGet)
        res.close()
        httpClient.close()
    }

    private fun checkIfTrigger(data: MutableMap<*, *>): Boolean? {
        val parameterValue = getValueByParamExpression(data, paramName) ?: return null
        return compareValueToLevel(parameterValue, alertCondition, level)
    }

    private fun getValueByParamExpression(data: Map<*, *>, expression: String): Any? {
        val keyPart = when (expression.contains(".")) {
            true -> expression.substring(0, expression.indexOf('.'))
            false -> expression
        }
        var action: String? = null
        val keyToGet = when (keyPart.contains(':')) {
            true -> {
                val splitIndex = keyPart.indexOf(":")
                action = keyPart.substring(splitIndex)
                keyPart.substring(0, splitIndex)
            }
            false -> keyPart
        }

        val dataValue = data[keyToGet]
        if (dataValue == null) {
            println("Metric map does not contain key: $keyToGet")
            return null
        }
        if (action == null) {
            return dataValue
        }

        return applyAction(dataValue, action)
    }

    private fun applyAction(dataValue: Any, action: String): Any? {
        return when (action) {
            "size" -> {
                when (dataValue) {
                    is List<*> -> dataValue.size
                    is Map<*, *> -> dataValue.size
                    else -> {
                        println("Unknown class to apply size for: ${dataValue.javaClass}")
                        null
                    }
                }
            }
            else -> {
                println("Unknown action to apply to data in alert: $action")
                null
            }
        }
    }

    private fun compareValueToLevel(value: Any, condition: String, level: String): Boolean? {
        return when (condition) {
            "<" -> compareLessThan(value, level)
            ">" -> compareBiggerThan(value, level)
            else -> {
                println("Unknown alert condition: $condition")
                null
            }
        }
    }

    private fun compareLessThan(value: Any, level: String): Boolean? {
        return when (value) {
            is String -> value.toDouble() < level.toDouble()
            is Int -> value < level.toInt()
            else -> {
                println("Unknown type for \"<\" comparison: ${value.javaClass}")
                null
            }
        }
    }

    private fun compareBiggerThan(value: Any, level: String): Boolean? {
        return when (value) {
            is String -> value.toDouble() > level.toDouble()
            is Int -> value > level.toInt()
            else -> {
                println("Unknown type for \">\" comparison: ${value.javaClass}")
                null
            }
        }
    }
}
