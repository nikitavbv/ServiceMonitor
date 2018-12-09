package com.github.nikitavbv.servicemonitor.metric

import java.lang.RuntimeException

class MetricNotFoundException : RuntimeException("Metric not found")
