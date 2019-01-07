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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

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
                .antMatchers(HttpMethod.POST, LOGIN_API).permitAll()
                .antMatchers(HttpMethod.POST, USER_API).permitAll()
                .antMatchers(AGENT_API).permitAll()
                .antMatchers(HttpMethod.POST, METRIC_API).permitAll()
                .antMatchers(HttpMethod.GET, INIT_API).permitAll()
                .antMatchers(HttpMethod.GET, STATUS_API).permitAll()
                .antMatchers(INSTALL_API).permitAll()
                .antMatchers(API_PATH_PATTERN).authenticated()
                .and()
                .addFilter(JWTAuthorizationFilter(securityProperties, authenticationManager()))
                .addFilterBefore(JWTAuthenticationFilter(securityProperties, authenticationManager()),
                        UsernamePasswordAuthenticationFilter::class.java)
    }

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth!!.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder())
    }
}
