package com.github.nikitavbv.servicemonitor

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FrontendTests {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun `test frontend index endpoint`() {
        val result = testRestTemplate.exchange("/", HttpMethod.GET, HttpEntity.EMPTY, String::class.java)
        assertNotNull(result)
        assertTrue(result.body!!.contains("<app-root></app-root>"))
    }

    @Test
    fun `test frontend other enpoints`() {
        val result = testRestTemplate.exchange("/auth", HttpMethod.GET, HttpEntity.EMPTY, String::class.java)
        assertNotNull(result)
        assertTrue(result.body!!.contains("<app-root></app-root>"))
    }
}
