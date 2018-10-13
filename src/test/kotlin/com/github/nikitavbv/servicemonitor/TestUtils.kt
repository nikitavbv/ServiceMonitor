package com.github.nikitavbv.servicemonitor

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException

@Throws(IOException::class)
fun <T> convertJSONStringToObject(json: String, objectClass: Class<T>): T {
    val mapper = ObjectMapper()
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

    val module = JavaTimeModule()
    mapper.registerModule(module)
    return mapper.readValue(json, objectClass)
}
