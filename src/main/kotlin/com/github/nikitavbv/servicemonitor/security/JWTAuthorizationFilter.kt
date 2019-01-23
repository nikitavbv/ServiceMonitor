package com.github.nikitavbv.servicemonitor.security

import com.github.nikitavbv.servicemonitor.SecurityProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.SignatureException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAuthorizationFilter(
    private val securityProperties: SecurityProperties,
    authManager: AuthenticationManager
) : BasicAuthenticationFilter(authManager) {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val header = request.getHeader(HEADER_STRING)

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(request, response)
            return
        }

        val authentication = getAuthentication(request)

        SecurityContextHolder.getContext().authentication = authentication
        chain.doFilter(request, response)
    }

    private fun getAuthentication(request: HttpServletRequest): Authentication? {
        val token = request.getHeader(HEADER_STRING)
        var authentication: Authentication? = null

        if (token != null) {
            var secret: String
            try {
                secret = securityProperties.secret
            } catch (e: UninitializedPropertyAccessException) {
                securityProperties.secret = securityProperties.generateSecret()
                println("Security secret is generated")
                secret = securityProperties.secret
            }

            try {
                val user = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .body
                    .subject

                if (user != null) {
                    authentication = UsernamePasswordAuthenticationToken(
                        user, null, emptyList<GrantedAuthority>()
                    )
                }
            } catch (e: SignatureException) {
                // ignore
            }
        }

        return authentication
    }
}
