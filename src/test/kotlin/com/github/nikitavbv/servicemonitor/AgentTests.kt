package com.github.nikitavbv.servicemonitor

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.nikitavbv.servicemonitor.agent.Agent
import com.github.nikitavbv.servicemonitor.agent.AgentRepository
import com.github.nikitavbv.servicemonitor.project.Project
import com.github.nikitavbv.servicemonitor.project.ProjectRepository
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.hamcrest.CoreMatchers.anything
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import java.lang.AssertionError
import javax.ws.rs.core.MediaType

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AgentTests {

    @Autowired
    lateinit var context: WebApplicationContext

    @Autowired
    lateinit var projectRepository: ProjectRepository

    @Autowired
    lateinit var agentRepository: AgentRepository

    lateinit var mockMvc: MockMvc

    @Before
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    @Transactional
    fun initAgentTest() {
        val project = Project(name = "demo project")
        projectRepository.save(project)
        assertEquals(0, project.agents.size)

        val agentsBeforeTest = agentRepository.count()

        val printer = ObjectMapper().writer().withDefaultPrettyPrinter()
        mockMvc.perform(post(AGENT_API)
            .contentType(MediaType.APPLICATION_JSON)
            .content(printer.writeValueAsBytes(mapOf(
                "token" to project.apiKey,
                "name" to "Ubuntu 18.04.1 LTS"
            ))))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", anything()))
            .andDo {
                val projectAfterUpdate = projectRepository.getOne(
                    project.id ?: throw AssertionError("Project id is not set")
                )
                assertEquals(1, projectAfterUpdate.agents.size)
                assertEquals("Ubuntu 18.04.1 LTS", projectAfterUpdate.agents[0].name)
                assertNotNull(projectAfterUpdate.agents[0].apiKey)
                assertEquals(agentsBeforeTest + 1, agentRepository.count())
                assertNotNull(agentRepository.findByApiKey(
                    project.agents[0].apiKey ?: throw AssertionError("No api key set for agent")
                ))
            }
    }

    @Test
    fun initAgentWithoutPropertyKey() {
        val printer = ObjectMapper().writer().withDefaultPrettyPrinter()
        mockMvc.perform(post(AGENT_API)
            .contentType(MediaType.APPLICATION_JSON)
            .content(printer.writeValueAsBytes(mapOf(
                "description" to "Ubuntu 18.04.1 LTS"
            ))))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error", equalTo("missing_parameter")))
            .andExpect(jsonPath("$.missing_parameter", equalTo("token")))
    }

    @Test
    fun testAgentAPIKeyGeneration() {
        val agent = Agent()
        TestCase.assertNull(agent.apiKey)
        agentRepository.save(agent)
        assertNotNull(agent.apiKey)

        val apiKey = agent.apiKey
        val agentSecondInstance = agentRepository.getOne(
            agent.id ?: throw java.lang.AssertionError("Agent id is not set")
        )
        assertNotNull(agentSecondInstance.apiKey)
        assertEquals(apiKey, agentSecondInstance.apiKey)
        agentRepository.save(agent)
        assertEquals(apiKey, agentSecondInstance.apiKey)
    }
}
