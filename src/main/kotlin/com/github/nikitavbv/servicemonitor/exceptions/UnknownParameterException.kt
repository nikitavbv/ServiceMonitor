package com.github.nikitavbv.servicemonitor.exceptions

import java.lang.RuntimeException

class UnknownParameterException(val parameterName: String) : RuntimeException("Unknown parameter: $parameterName")
