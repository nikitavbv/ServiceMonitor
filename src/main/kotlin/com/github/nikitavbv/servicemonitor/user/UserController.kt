package com.github.nikitavbv.servicemonitor.user

import com.github.nikitavbv.servicemonitor.security.PermissionDeniedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping("/users")
class UserController (
    val userRepository: ApplicationUserRepository,
    val bCryptPasswordEncoder: BCryptPasswordEncoder
) {

    @Autowired
    lateinit var applicationUserRepository: ApplicationUserRepository

    @PostMapping
    fun signUp(httpRequest: HttpServletRequest, @RequestBody @Valid user: ApplicationUser): SignUpResult {
        user.password = bCryptPasswordEncoder.encode(user.password)
        if (httpRequest.remoteUser != null) {
            val requestUser = applicationUserRepository.findByUsername(httpRequest.remoteUser)
            if (!requestUser.isAdmin && user.isAdmin) {
                throw PermissionDeniedException("Non-admin users are not allowed to create admin users")
            }
        } else if (applicationUserRepository.count() != 0L) {
            throw PermissionDeniedException("Auth required for creating users")
        }
        userRepository.save(user)
        return SignUpResult(user.id ?: throw RuntimeException("Failed to save user"))
    }

}