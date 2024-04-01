package com.example.p2p_project.services

import com.example.p2p_project.config.MyUserDetails
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class AuthenticationService {
    fun authenticateUser(username: String?, password: String?) {
        // Создайте объект Authentication для пользователя
        val authentication: Authentication = UsernamePasswordAuthenticationToken(username, password)

        // Установите аутентификацию в SecurityContext
        SecurityContextHolder.getContext().authentication = authentication
    }


    fun getUserDetails(authentication: Authentication): MyUserDetails {
        return authentication.principal as MyUserDetails
    }
}
