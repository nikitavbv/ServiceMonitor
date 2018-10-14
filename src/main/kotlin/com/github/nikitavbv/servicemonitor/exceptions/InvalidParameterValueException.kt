package com.github.nikitavbv.servicemonitor.exceptions

import java.lang.RuntimeException

class InvalidParameterValueException : RuntimeException {

    var parameterName: String
    var parameterMessage: String? = null

    constructor(parameterName: String): super("Invalid value of $parameterName") {
        this.parameterName = parameterName
    }

    constructor(parameterName: String, message: String): super("Invalid value of $parameterName: $message") {
        this.parameterName = parameterName
        this.parameterMessage = message
    }
}
