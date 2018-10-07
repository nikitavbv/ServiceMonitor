package com.github.nikitavbv.servicemonitor

import com.github.nikitavbv.servicemonitor.user.ApplicationUser
import com.github.nikitavbv.servicemonitor.user.ApplicationUserRepository
import junit.framework.TestCase.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.http.*

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthTests {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var applicationUserRepository: ApplicationUserRepository

    @Test
    fun `sign up with empty request`() {
        val headers = HttpHeaders()
        headers.add("Content-type", "application/json")
        val result = testRestTemplate.exchange(
                "/users/signup",
                HttpMethod.POST,
                HttpEntity("not_a_json", headers),
                Map::class.java
        )
        assertNotNull(result)
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertEquals("failed to parse request", result.body?.get("error"))
    }

    @Test
    fun `sign up with incomplete request`() {
        val headers = HttpHeaders()
        headers.add("Content-type", "application/json")
        val result = testRestTemplate.exchange(
                "/users/signup",
                HttpMethod.POST,
                HttpEntity("{\"username\": \"admin\"}", headers), // no password
                Map::class.java
        )
        assertNotNull(result)
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertEquals("missing parameter", result.body?.get("error"))
        assertEquals("password", result.body?.get("missing_parameter"))
    }

    @Test
    fun `sign up`() {
        val usersBeforeSignUp = applicationUserRepository.count()
        val signUpHeaders = HttpHeaders()
        signUpHeaders.add("Content-type", "application/json")
        val result = testRestTemplate.exchange(
                "/users/signup",
                HttpMethod.POST,
                HttpEntity("""{
                    "username": "admin",
                    "password": "password"
                }""".trimIndent(), signUpHeaders),
                Map::class.java
        )
        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertTrue(result.body?.contains("userID")!!)
        assertEquals(usersBeforeSignUp+1, applicationUserRepository.count())

        applicationUserRepository.deleteById(result.body?.get("userID").toString().toLong())
        assertEquals(usersBeforeSignUp, applicationUserRepository.count())
    }

    @Test
    fun `login with correct credentials`() {
        val user = ApplicationUser(
                username="admin",
                password="\$2a\$10\$HEXlSrByZSsRozCyDCil1uZmXF1u2v6ky9UdXNkv7u6KdsZVujFZ2" // "password"
        )
        applicationUserRepository.save(user)

        val loginHeaders = HttpHeaders()
        loginHeaders.add("Content-type", "application/json")
        val result = testRestTemplate.exchange(
                "/login",
                HttpMethod.POST,
                HttpEntity("""{
                    "username": "admin",
                    "password": "password"
                }""".trimMargin(), loginHeaders),
                Map::class.java
        )
        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertTrue(result.headers.containsKey("Authorization"))
        val authHeader = result.headers["Authorization"]?.get(0)
        assertTrue(authHeader?.startsWith("Bearer")!!)

        // verify auth by making request to protected route
        val verifyHeaders = HttpHeaders()
        verifyHeaders.add("Content-type", "application/json")
        verifyHeaders.add("Authorization", authHeader)
        val verifyResult = testRestTemplate.exchange(
                "/",
                HttpMethod.GET,
                HttpEntity("", verifyHeaders),
                String::class.java
        )
        assertNotNull(verifyResult)
        println(verifyResult.body)
        assertEquals(HttpStatus.OK, verifyResult.statusCode)
        assertNotNull(verifyResult.body)
        assertTrue(verifyResult.body?.isNotEmpty()!!)

        applicationUserRepository.delete(user)
    }

    @Test
    fun `login with wrong username`() {
        val user = ApplicationUser(
                username="admin",
                password="\$2a\$10\$HEXlSrByZSsRozCyDCil1uZmXF1u2v6ky9UdXNkv7u6KdsZVujFZ2" // "password"
        )
        applicationUserRepository.save(user)

        val loginHeaders = HttpHeaders()
        loginHeaders.add("Content-type", "application/json")
        val result = testRestTemplate.exchange(
                "/login",
                HttpMethod.POST,
                HttpEntity("""{
                    "username": "wrong_username",
                    "password": "password"
                }""".trimMargin(), loginHeaders),
                Map::class.java
        )
        assertNotNull(result)
        assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        applicationUserRepository.delete(user)
    }

    @Test
    fun `login with wrong password`() {
        val user = ApplicationUser(
                username="admin",
                password="\$2a\$10\$HEXlSrByZSsRozCyDCil1uZmXF1u2v6ky9UdXNkv7u6KdsZVujFZ2" // "password"
        )
        applicationUserRepository.save(user)

        val loginHeaders = HttpHeaders()
        loginHeaders.add("Content-type", "application/json")
        val result = testRestTemplate.exchange(
                "/login",
                HttpMethod.POST,
                HttpEntity("""{
                    "username": "admin",
                    "password": "wrong_password"
                }""".trimMargin(), loginHeaders),
                Map::class.java
        )
        assertNotNull(result)
        assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        applicationUserRepository.delete(user)
    }

    @Test
    fun `access denied for protected routes`() {
        val result = testRestTemplate.getForEntity("/", String::class.java)
        assertNotNull(result)
        assertEquals(HttpStatus.FORBIDDEN, result.statusCode)
        assertTrue(result.body!!.contains("Access Denied"))
    }

}
