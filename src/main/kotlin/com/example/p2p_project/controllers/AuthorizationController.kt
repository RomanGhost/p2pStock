package com.example.p2p_project.controllers

import com.example.p2p_project.models.User
import com.example.p2p_project.services.UserService
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
class AuthorizationController(val userService: UserService) {
    @Value("\${application.info.api}")
    private lateinit var link: String

    @GetMapping("/sign-up")
    fun showSignUp(model:Model):String{
        val user:User = User()
        model.addAttribute("link", link)
        model.addAttribute("user", user)
        return "signUp"
    }

    @PostMapping("/sign-up/save")
    fun signUp(@ModelAttribute("user")newUser:User,
                     result:BindingResult,
                     session:HttpSession):String{
        var user = userService.getByLogin(newUser.login)

        // В случае, если пользователь найден, переотправить на sign-in
        if (user != null){
            return "redirect:${link}/sign-in"
        }

        user = userService.add(newUser)
        session.setAttribute("user", user)

        return "redirect:${link}/welcome"
    }


    @GetMapping("/sign-in")
    fun showSignIn(model:Model):String{
        val user:User = User()
        model.addAttribute("link", link)
        model.addAttribute("user", user)
        return "signIn"
    }

    @PostMapping("/sign-in/check")
    fun signIn(@ModelAttribute("user")newUser:User,
               result:BindingResult,
               session:HttpSession,
               model:Model):String{
        val user = userService.getByLogin(newUser.login) ?: return "redirect:${link}/sign-up"

        val password = userService.isValidPassword(user, newUser)
        //user don't exist
        //user password is not correct
        if(!password) {
            result.rejectValue("password", "error.user", "Incorrect password")
            return "signIn"
        }

        session.setAttribute("user", user)
        return "redirect:$link/welcome"
    }


    //TODO("Убрать данный контроллер в другой контроллер")
    @GetMapping("/welcome")
    fun welcomePage(model:Model, session: HttpSession):String{
        val user = session.getAttribute("user")
        model.addAttribute("user", user)
        return "welcome"
    }

}