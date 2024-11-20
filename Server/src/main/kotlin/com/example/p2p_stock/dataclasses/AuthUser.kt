package com.example.p2p_stock.dataclasses

data class LoginUser(
    val email:String,
    val password: String,
)

data class RegisterUser(
    val login:String,
    val email:String,
    val password: String,
)

data class JwtToken(
    val token:String
)