package com.github.nikitavbv.servicemonitor

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.nikitavbv.servicemonitor.project.ProjectRepository
import junit.framework.TestCase.assertEquals
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.anything
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectTests {

    @Autowired
    lateinit var context: WebApplicationContext

    @Autowired
    lateinit var projectRepository: ProjectRepository

    lateinit var mockMvc: MockMvc

    @Before
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    @WithMockUser
    fun `create project`() {
        val projectsBeforeTest = projectRepository.count()
        val printer = ObjectMapper().writer().withDefaultPrettyPrinter()
        mockMvc.perform(post(PROJECT_API)
            .contentType(MediaType.APPLICATION_JSON)
            .content(printer.writeValueAsBytes(mapOf(
                "name" to "Project name"
            ))))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", anything()))
            .andExpect(jsonPath("$.name", equalTo("Project name")))
            .andDo {
                assertEquals(projectsBeforeTest + 1, projectRepository.count())
            }
    }
}