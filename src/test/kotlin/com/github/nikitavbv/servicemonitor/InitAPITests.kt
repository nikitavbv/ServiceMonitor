package com.github.nikitavbv.servicemonitor

import com.github.nikitavbv.servicemonitor.user.ApplicationUser
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class InitAPITests {

    @Autowired
    lateinit var context: WebApplicationContext

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
    fun `init call with no setup`() {
        mockMvc.perform(get(INIT_API))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status", equalTo("setup_required")))
    }

    @Test
    fun `init call with admin user`() {
        val user = ApplicationUser(
            username = "admin",
            password = "\$2a\$10\$HEXlSrByZSsRozCyDCil1uZmXF1u2v6ky9UdXNkv7u6KdsZVujFZ2" // "password"
        )
        userRepository.save(user)

        mockMvc.perform(get(INIT_API))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status", equalTo("auth_required")))
            .andDo { userRepository.delete(user) }
    }

    @Test
    @WithMockUser("admin")
    fun `init call with admin user and auth`() {
        val user = ApplicationUser(
            username = "admin",
            password = "\$2a\$10\$HEXlSrByZSsRozCyDCil1uZmXF1u2v6ky9UdXNkv7u6KdsZVujFZ2" // "password"
        )
        userRepository.save(user)

        mockMvc.perform(get(INIT_API))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status", equalTo("ok")))
            .andDo { userRepository.delete(user) }
    }
}
