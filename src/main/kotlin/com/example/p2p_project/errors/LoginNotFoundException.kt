package com.example.p2p_project.errors

import org.springframework.security.core.AuthenticationException

class LoginNotFoundException(message:String): AuthenticationException(message) {
}