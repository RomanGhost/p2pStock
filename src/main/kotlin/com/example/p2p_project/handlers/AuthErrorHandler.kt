package com.example.p2p_project.handlers

import com.example.p2p_project.errors.LoginNotFoundException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

class AuthErrorHandler(private val apiLink:String): AuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        exception: AuthenticationException?
    ) {
        println("Login Exception $exception")
        when(exception){
            is BadCredentialsException -> response.sendRedirect("${apiLink}/sign-in?error")
            is LoginNotFoundException -> response.sendRedirect("${apiLink}/sign-up")
            else -> println("WTF?")
        }
    }
}