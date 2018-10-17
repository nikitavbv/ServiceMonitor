package com.github.nikitavbv.servicemonitor

import com.github.nikitavbv.servicemonitor.security.JWTAuthenticationFilter
import com.github.nikitavbv.servicemonitor.security.JWTAuthorizationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
@EnableWebSecurity
class WebSecurity(
    val userDetailsService: UserDetailsService,
    val securityProperties: SecurityProperties
) : WebSecurityConfigurerAdapter() {

    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    override fun configure(http: HttpSecurity) {
        http.csrf().disable().authorizeRequests()
                .antMatchers(HttpMethod.POST, "/login").permitAll()
                .antMatchers(HttpMethod.POST, USER_API).permitAll()
                .antMatchers(HttpMethod.POST, AGENT_API).permitAll()
                .antMatchers(HttpMethod.POST, METRIC_API).permitAll()
                .antMatchers(HttpMethod.GET, INIT_API).permitAll()
                .antMatchers(API_PATH_PATTERN).authenticated()
                .and()
                .addFilter(JWTAuthenticationFilter(securityProperties, authenticationManager()))
                .addFilter(JWTAuthorizationFilter(securityProperties, authenticationManager()))
    }

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth!!.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder())
    }
}
