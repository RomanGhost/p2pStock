package com.example.p2p_project.controllers.admin

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.services.UserService
import com.example.p2p_project.services.dataServices.UserRoleService
import org.springframework.security.core.Authentication
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
    @GetMapping("/user_management")
    fun getAddUserManagement(model: Model): String {
        val allUsers = userService.getUserWithoutRole("Менеджер")
        model.addAttribute("users", allUsers)

        return "addManager"
    }

    @PostMapping("/add_manager")
    fun postAddManager(@RequestParam userId: Long, authentication: Authentication): String {
        val userDetails = authentication.principal as MyUserDetails
        val authUserId = userDetails.user.id

        if (authUserId == userId)
            return "redirect:/admin_panel/user_management"

        val user = userService.getById(userId)
        roleService.addUserRole(user, "Менеджер")

        return "redirect:/admin_panel/user_management"
    }

    @PostMapping("/block")
    fun postBlockUser(@RequestParam userId: Long, authentication: Authentication): String {
        val userDetails = authentication.principal as MyUserDetails
        val authUserId = userDetails.user.id
        if (authUserId == userId)
            return "redirect:/admin_panel/user_management"

        userService.disableUser(userId)

        return "redirect:/admin_panel/user_management"
    }

    @PostMapping("/active")
    fun postActiveUser(@RequestParam userId: Long, authentication: Authentication): String {
        val userDetails = authentication.principal as MyUserDetails
        val authUserId = userDetails.user.id
        if (authUserId == userId)
            return "redirect:/admin_panel/user_management"

        userService.activeUser(userId)

        return "redirect:/admin_panel/user_management"
    }
}
