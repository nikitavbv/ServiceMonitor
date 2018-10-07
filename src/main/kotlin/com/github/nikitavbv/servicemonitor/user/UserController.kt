package com.github.nikitavbv.servicemonitor.user

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException
import javax.validation.Valid

@RestController
@RequestMapping("/users")
class UserController (
    val userRepository: ApplicationUserRepository,
    val bCryptPasswordEncoder: BCryptPasswordEncoder
) {

    @PostMapping("/signup")
    fun signUp(@RequestBody @Valid user: ApplicationUser): SignUpResult {
        user.password = bCryptPasswordEncoder.encode(user.password)
        userRepository.save(user)
        return SignUpResult(user.id ?: throw RuntimeException("Failed to save user"))
    }

}