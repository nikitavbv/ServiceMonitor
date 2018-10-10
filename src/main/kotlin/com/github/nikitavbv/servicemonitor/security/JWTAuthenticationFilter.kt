package com.github.nikitavbv.servicemonitor.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.nikitavbv.servicemonitor.SecurityProperties
import com.github.nikitavbv.servicemonitor.user.ApplicationUser
import io.jsonwebtoken.Jwts
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.io.Decoders
import java.util.Date

class JWTAuthenticationFilter(
    private val securityProperties: SecurityProperties,
    authManager: AuthenticationManager
) : UsernamePasswordAuthenticationFilter() {

    init {
        authenticationManager = authManager
    }

    @Throws(AuthenticationException::class, IOException::class, ServletException::class)
    override fun attemptAuthentication(
        req: HttpServletRequest,
        res: HttpServletResponse
    ): Authentication {
        val creds = ObjectMapper().readValue(req.inputStream, ApplicationUser::class.java)
        return authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                        creds.username,
                        creds.password,
                        emptyList<GrantedAuthority>()
                )
        )
    }

    @Throws(IOException::class, ServletException::class)
    override fun successfulAuthentication(
        req: HttpServletRequest,
        res: HttpServletResponse,
        chain: FilterChain?,
        auth: Authentication
    ) {
        val keyBytes = Decoders.BASE64.decode(securityProperties.secret)
        val key = Keys.hmacShaKeyFor(keyBytes)

        val jwt = Jwts.builder()
                .setSubject((auth.principal as User).username)
                .setExpiration(Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact()

        res.addHeader(HEADER_STRING, "$TOKEN_PREFIX $jwt")
    }
}