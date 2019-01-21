package com.github.nikitavbv.servicemonitor.metric

abstract class AbstractMetric {

    abstract fun asMap(): Map<String, Any?>

}
