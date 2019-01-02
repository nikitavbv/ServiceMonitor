package com.github.nikitavbv.servicemonitor.alert

import com.github.nikitavbv.servicemonitor.metric.Metric
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
data class Alert (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var rule: String? = null,
    var action: String? = null,
    var triggered: Boolean = false,

    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id")
    val metric: Metric? = null
) {

    fun runCheck(data: MutableMap<*, *>, alertRepository: AlertRepository) {
        val state = checkIfTrigger(data)
        if (state == triggered) {
            return
        }

        if (state) {
            runAction()
        }

        triggered = state
        alertRepository.save(this)
    }

    private fun runAction() {
        // TODO: implement this
    }

    private fun checkIfTrigger(data: MutableMap<*, *>): Boolean {
        // TODO: implement check
        return false
    }

}
