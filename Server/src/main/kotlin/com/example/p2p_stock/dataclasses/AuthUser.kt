package com.example.p2p_stock.dataclasses

import io.jsonwebtoken.security.Password

data class LoginUser(
    val email:String,
    val password: String,
)

data class RegisterUser(
    val username:String,
    val email:String,
    val password: String,
)

data class JwtToken(
    val token:String
)