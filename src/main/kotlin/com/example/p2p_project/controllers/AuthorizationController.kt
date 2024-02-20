package com.example.p2p_project.controllers

import com.example.p2p_project.models.User
import com.example.p2p_project.services.AuthenticationService
import com.example.p2p_project.services.UserRoleService
import com.example.p2p_project.services.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping("\${application.info.api}")
class AuthorizationController(
    val userService: UserService,
    val userRoleService: UserRoleService,
    val authenticationService:AuthenticationService
) {
    @Value("\${application.info.api}")
    private lateinit var apiLink: String

    @GetMapping("/sign-up")
    fun showSignUp(model:Model):String{
        val user:User = User()
        model.addAttribute("link", apiLink)
        model.addAttribute("user", user)
        return "signUp"
    }

    @PostMapping("/sign-up/save")
    fun signUp(@ModelAttribute("user")newUser:User,
               result:BindingResult,
               session:HttpSession,
               request: HttpServletRequest
    ):String{
        val user = userService.getByLogin(newUser.login)

        // В случае, если пользователь найден, переотправить на sign-in
        if (user != null){
            return "redirect:${apiLink}/sign-in"
        }

        if (newUser.password.length < 8) {
            result.rejectValue("password", "error.user", "Password must be at least 8 characters long")
        }
        if (newUser.login.length < 2) {
            result.rejectValue("login", "error.user", "Login must be at least 2 characters long")
        }

        val registerUser  = userService.add(newUser)
        userRoleService.addUserRole(registerUser, "Пользователь")

        //TODO("Реализовать вход пользователя в личный кабинет без формы входа")
        authenticationService.authenticateUser(registerUser.login, registerUser.password);

        return "redirect:${apiLink}/account/welcome"
    }

    @GetMapping("/sign-in")
    fun showSignIn(model:Model):String{
        model.addAttribute("link", apiLink)
        return "signIn"
    }

}