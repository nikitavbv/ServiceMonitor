package com.github.nikitavbv.servicemonitor.metric

import com.github.nikitavbv.servicemonitor.exceptions.InvalidParameterValueException
import com.github.nikitavbv.servicemonitor.exceptions.MissingParameterException

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
