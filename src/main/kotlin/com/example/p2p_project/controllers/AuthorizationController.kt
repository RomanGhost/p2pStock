package com.example.p2p_project.controllers

import com.example.p2p_project.models.User
import com.example.p2p_project.services.AuthenticationService
import com.example.p2p_project.services.UserRoleService
import com.example.p2p_project.services.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes


@Controller
@RequestMapping("\${application.info.appLink}")
class AuthorizationController(
    val userService: UserService,
    val userRoleService: UserRoleService,
    val authenticationService:AuthenticationService
) {
   @Value("\${application.info.appLink}")
    private lateinit var appLink: String

    @GetMapping("/sign-up")
    fun showSignUp(model:Model, redirectAttributes: RedirectAttributes):String{
        val user:User = User()
        model.addAttribute("link", appLink)
        model.addAttribute("user", user)
        return "signUp"
    }

    @PostMapping("/sign-up/save")
    fun signUp(@ModelAttribute("user")newUser:User,
               result:BindingResult, model: Model,
               redirectAttributes:RedirectAttributes
    ):String{
        val user = userService.getByLogin(newUser.login)

        // В случае, если пользователь найден, переотправить на sign-in
        if (user != null){
            return "redirect:${appLink}/sign-in"
        }

        model.addAttribute("link", appLink)
        if (newUser.password.length < 8) {
            redirectAttributes.addFlashAttribute("errorMessage", "Card number is too short")
            return "redirect:${appLink}/sign-in"
        }
        if (newUser.login.length < 2) {
            redirectAttributes.addFlashAttribute("errorMessage", "Card number is too short")
            return "redirect:${appLink}/sign-in"
        }

        val registerUser  = userService.add(newUser)
        userRoleService.addUserRole(registerUser, "Пользователь")

        //TODO("Реализовать вход пользователя в личный кабинет без формы входа")
        authenticationService.authenticateUser(registerUser.login, registerUser.password);

        return "redirect:${appLink}/account/welcome"
    }

    @GetMapping("/sign-in")
    fun showSignIn(model:Model):String{
        model.addAttribute("link", appLink)
        return "signIn"
    }

}