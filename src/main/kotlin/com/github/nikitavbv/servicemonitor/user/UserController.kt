package com.github.nikitavbv.servicemonitor.user

import com.github.nikitavbv.servicemonitor.USER_API
import com.github.nikitavbv.servicemonitor.security.PermissionDeniedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping(USER_API)
class UserController(
    val userRepository: ApplicationUserRepository,
    val bCryptPasswordEncoder: BCryptPasswordEncoder
) {

    @Autowired
    lateinit var applicationUserRepository: ApplicationUserRepository

    @GetMapping
    fun getUserInfo(httpRequest: HttpServletRequest): ApplicationUser {
        return applicationUserRepository.findByUsername(httpRequest.remoteUser)
    }

    @PostMapping
    fun signUp(httpRequest: HttpServletRequest, @RequestBody @Valid user: ApplicationUser): SignUpResult {
        user.password = bCryptPasswordEncoder.encode(user.password)
        val requestUser: ApplicationUser? = when (httpRequest.remoteUser) {
            null -> null
            else -> applicationUserRepository.findByUsername(httpRequest.remoteUser)
        }
        checkCreateUserPermissions(user, requestUser)
        userRepository.save(user)
        return SignUpResult(user.id ?: throw SignUpException("Failed to save user"))
    }

    fun checkCreateUserPermissions(userToCreate: ApplicationUser, creator: ApplicationUser?) {
        if (creator != null) {
            if (!creator.isAdmin && userToCreate.isAdmin) {
                throw PermissionDeniedException("Non-admin users are not allowed to create admin users")
            }
        } else if (applicationUserRepository.count() != 0L) {
            throw PermissionDeniedException("Auth required for creating users")
        }
    }

}
