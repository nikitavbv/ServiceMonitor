package com.github.nikitavbv.servicemonitor

import com.github.nikitavbv.servicemonitor.exceptions.MissingAPIKeyException
import com.github.nikitavbv.servicemonitor.exceptions.MissingParameterException

fun getRequiredBodyParameter(body: Map<String, Any>, parameterName: String): Any? {
    return body[parameterName] ?: throw MissingParameterException(parameterName)
}

fun getRequestAPIToken(body: Map<String, Any>): String {
    return (body["token"] ?: throw MissingAPIKeyException()).toString()
}
