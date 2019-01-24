package com.github.nikitavbv.servicemonitor

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.nikitavbv.servicemonitor.project.CreateProjectResult
import com.github.nikitavbv.servicemonitor.project.Project
import com.github.nikitavbv.servicemonitor.project.ProjectRepository
import com.github.nikitavbv.servicemonitor.user.ApplicationUser
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
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
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectTests {

    @Autowired
    lateinit var context: WebApplicationContext

    @Autowired
    lateinit var projectRepository: ProjectRepository

    @Autowired
    lateinit var userRepository: ApplicationUserRepository

    lateinit var mockMvc: MockMvc

    @Before
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    @WithMockUser("admin")
    @Transactional
    fun createProject() {
        val user = ApplicationUser(
            username = "admin",
            password = "\$2a\$10\$HEXlSrByZSsRozCyDCil1uZmXF1u2v6ky9UdXNkv7u6KdsZVujFZ2" // "password"
        )
        userRepository.save(user)

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
                result ->
                run {
                    val projectInfo = convertJSONStringToObject(
                        result.response.contentAsString,
                        CreateProjectResult::class.java
                    )
                    assertEquals(projectsBeforeTest + 1, projectRepository.count())
                    assertEquals("Project name", projectInfo.name)

                    val project = projectRepository.getOne(
                        projectInfo.id ?: throw AssertionError("No id set for project")
                    )
                    assertNotNull(project.apiKey)
                    project.users.clear()
                    projectRepository.delete(project)
                }
            }
    }

    @Test
    fun testProjectAPIKeyGeneration() {
        val project = Project(name = "Test project")
        assertNull(project.apiKey)
        projectRepository.save(project)
        assertNotNull(project.apiKey)

        val apiKey = project.apiKey
        val projectSecondInstance = projectRepository.getOne(
            project.id ?: throw java.lang.AssertionError("Project id is not set")
        )
        assertNotNull(projectSecondInstance.apiKey)
        assertEquals(apiKey, projectSecondInstance.apiKey)
        projectRepository.save(project)
        assertEquals(apiKey, projectSecondInstance.apiKey)
    }
}
