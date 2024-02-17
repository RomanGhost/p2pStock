package com.example.p2p_project.handlers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

class AuthErrorHandler(val apiLink:String): AuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AuthenticationException?
    ) {
        print("Login Exception $exception")
        if(exception is BadCredentialsException){
            response?.sendRedirect("${apiLink}/sign-in?error")
        }
        if(exception is Exception){
            response?.sendRedirect("${apiLink}/sign-up")
        }
    }
}