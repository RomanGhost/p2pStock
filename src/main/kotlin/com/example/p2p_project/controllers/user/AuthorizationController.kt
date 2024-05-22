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
    val authenticationService: AuthenticationService
) {
    @GetMapping("/sign-in")
    fun showSignIn(model: Model): String {
        return "signIn"
    }

    @GetMapping("/sign-up")
    fun showSignUp(
        model: Model,
        redirectAttributes: RedirectAttributes,
        @RequestParam(name = "error", required = false) error: String? = null
    ): String {
        val user: User = User()
        if (error != null)
            model.addAttribute("errorMessage", "Пользователь не найден")
        model.addAttribute("user", user)
        return "signUp"
    }

    @PostMapping("/sign-up/save")
    fun signUp(
        @ModelAttribute("user") newUser: User,
        @RequestParam("second_password") secondPassword: String,
        result: BindingResult, model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        val user = userService.getByLogin(newUser.login)
        if (user != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пользователь с таким логином уже существует")
            return "redirect:/auth/sign-up"
        }

        if (newUser.password.length < 8) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пароль короткий, минимум 8 символов")
            return "redirect:/auth/sign-up"
        }

        if (newUser.login.trim().length < 2) {
            redirectAttributes.addFlashAttribute("errorMessage", "Логин слишком короткий, минимум 2 символа")
            return "redirect:/auth/sign-up"
        }

        if (newUser.password != secondPassword) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пароли не совпадают")
            return "redirect:/auth/sign-up"
        }

        val registeredUser = userService.add(newUser)
        userRoleService.addUserRole(registeredUser, "Пользователь")
        authenticationService.authenticateUser(registeredUser.login, registeredUser.password)

        return "redirect:/platform/account/welcome"
    }
}
