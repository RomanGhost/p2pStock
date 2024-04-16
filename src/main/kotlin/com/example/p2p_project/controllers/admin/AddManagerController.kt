package com.example.p2p_project.controllers.admin

import com.example.p2p_project.services.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping("/admin_panel")
class AddManagerController(val userService: UserService) {
    @GetMapping("/add_manager")
    fun getAddManager(model: Model):String{
        val allUsers = userService.getAll()
        model.addAttribute("users", allUsers)

        return "addManager"
    }
}
