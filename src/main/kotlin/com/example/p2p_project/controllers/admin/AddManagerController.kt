package com.example.p2p_project.controllers.admin

import com.example.p2p_project.services.UserService
import com.example.p2p_project.services.dataServices.UserRoleService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam


@Controller
@RequestMapping("/admin_panel")
class AddManagerController(
    val userService: UserService,
    val roleService: UserRoleService

) {
    @GetMapping("/add_manager")
    fun getAddManager(model: Model):String{
        val allUsers = userService.getUserWithoutRole("Менеджер")
        model.addAttribute("users", allUsers)

        return "addManager"
    }

    @PostMapping("/add_manager")
    fun postAddManager(@RequestParam userId:Long):String{
        val user = userService.getById(userId)
        roleService.addUserRole(user, "Менеджер")

        return "redirect:/admin_panel/add_manager"
    }
}
