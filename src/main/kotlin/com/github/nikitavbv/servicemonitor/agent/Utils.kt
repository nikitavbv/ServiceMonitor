package com.github.nikitavbv.servicemonitor.agent

import com.github.nikitavbv.servicemonitor.exceptions.MissingParameterException

fun getRequiredBodyParameter(body: Map<String, Any>, parameterName: String): Any? {
    return body[parameterName] ?: throw MissingParameterException(parameterName)
}
