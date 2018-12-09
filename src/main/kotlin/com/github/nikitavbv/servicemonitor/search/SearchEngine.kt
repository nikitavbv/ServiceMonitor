package com.github.nikitavbv.servicemonitor.search

import com.github.nikitavbv.servicemonitor.agent.Agent
import com.github.nikitavbv.servicemonitor.metric.Metric
import com.github.nikitavbv.servicemonitor.project.Project

class SearchEngine(val projectList: List<Project> ) {

    fun doSearchFor(query: String) {
        val tokens = tokenize(query)
        val wordsToContain: MutableList<String> = mutableListOf()
        val metricsChecks: MutableMap<String, String> = mutableMapOf()
        tokens.forEach { token ->
            val conditionPos = getConditionPos(token)
            if (token.startsWith(":") && conditionPos != null) {
                // TODO: add matching by metrics
            } else {
                wordsToContain.add(token)
            }
        }

        projectList.map { project ->
            project.agents.map { agent ->
                if (matchAgentByWords(agent, wordsToContain) || wordsToContain.size == 0) {
                    agent.metrics.toMutableList()
                } else {
                    mutableListOf()
                }
            }.reduce { a, b ->
                a.addAll(b)
                a
            }
        }.reduce { a, b ->
            a.addAll(b)
            a
        }
    }

    private fun matchMetricByChecks(metric: Metric, checks: MutableMap<String, String>): Boolean {
        // TODO: implement this
        return true
    }

    private fun matchAgentByWords(agent: Agent, words: List<String>): Boolean {
        words.forEach { word ->
            val agentName = agent.name
            if (agentName != null && agentName.contains(word)) return true
        }
        return false
    }

    private fun getConditionPos(token: String): Pair<String, Int>? {
        val pos = listOf(">", "=", "<", ">=", "<=").map { it to token.indexOf(it) }.filter {
            it.second != -1
        }
        return if (pos.isEmpty()) {
            null
        } else {
            pos.reduce { a, b ->
                return if (a.second < b.second) {
                    a
                } else {
                    b
                }
            }
        }
    }

    private fun tokenize(q: String): List<String> {
        val result: MutableList<String> = mutableListOf()
        var tokenStartedAt = 0
        var quotesOpened = false
        val str = q.trim().replace(" = ", "=").replace("  ", " ")
        for (i in 0 until str.length) {
            if (str[i] == '"' && (i == 0 || str[i - 1] != '\\')) {
                quotesOpened = !quotesOpened
            } else if (str[i] == ' ' && !quotesOpened) {
                result.add(str.substring(tokenStartedAt, i))
                tokenStartedAt = i + 1
            }
        }
        return result
    }

}
