package com.example.p2p_project.controllers.user

import com.example.p2p_project.models.User
import com.example.p2p_project.services.AuthenticationService
import com.example.p2p_project.services.UserService
import com.example.p2p_project.services.dataServices.UserRoleService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes


@Controller
@RequestMapping("/auth")
class AuthorizationController(
    val userService: UserService,
    val userRoleService: UserRoleService,
    val authenticationService:AuthenticationService
) {


    @GetMapping("/sign-up")
    fun showSignUp(
        model: Model,
        redirectAttributes: RedirectAttributes,
        @RequestParam(name = "error", required = false) error: String? = null
    ): String {
        val user:User = User()
        if (error != null)
            model.addAttribute("errorMessage", "Пользователь не найден")
        model.addAttribute("user", user)
        return "signUp"
    }

    @PostMapping("/sign-up/save")
    fun signUp(@ModelAttribute("user")newUser:User,
               result:BindingResult, model: Model,
               redirectAttributes:RedirectAttributes
    ):String{
        //TODO("Добавить второе поле повтор пароля(Не обязательно)")
        val user = userService.getByLogin(newUser.login)
        // В случае, если пользователь найден, переотправить на sign-in
        if (user != null){
            return "redirect:/auth/sign-in"
        }

        if (newUser.password.length < 8) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password is too short")
            return "redirect:/auth/sign-up"
        }
        if (newUser.login.length < 2) {
            redirectAttributes.addFlashAttribute("errorMessage", "Login length is too short")
            return "redirect:/auth/sign-up"
        }

        val registerUser  = userService.add(newUser)
        userRoleService.addUserRole(registerUser, "Пользователь")

        //TODO("Реализовать вход пользователя в личный кабинет без формы входа")
        authenticationService.authenticateUser(registerUser.login, registerUser.password)

        return "redirect:/platform/account/welcome"
    }

    @GetMapping("/sign-in")
    fun showSignIn(model:Model):String{
        return "signIn"
    }

}