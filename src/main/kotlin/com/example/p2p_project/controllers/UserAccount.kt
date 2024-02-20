package com.example.p2p_project.controllers

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.services.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${application.info.api}/account")

class UserAccount(private val userService: UserService) {
    @Value("\${application.info.api}")
    private lateinit var apiLink: String

    @GetMapping("/welcome")
    fun welcomePage(model: Model, authentication: Authentication):String{
        val userDetails = authentication.principal as MyUserDetails
        val login = userDetails.username?:"lol"
        model.addAttribute("login", login)
        return "welcome"
    }

}