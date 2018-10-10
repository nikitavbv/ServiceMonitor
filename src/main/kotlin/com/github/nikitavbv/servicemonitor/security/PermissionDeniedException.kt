package com.github.nikitavbv.servicemonitor.security

import java.lang.RuntimeException

class PermissionDeniedException(msg: String) : RuntimeException(msg)
