package com.github.nikitavbv.servicemonitor.exceptions

import java.lang.RuntimeException

class MissingParameterException(val parameterName: String) : RuntimeException("Missing parameter: $parameterName")
