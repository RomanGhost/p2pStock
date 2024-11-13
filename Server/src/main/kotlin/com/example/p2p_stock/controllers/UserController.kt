package com.example.p2p_stock.controllers

import com.example.p2p_stock.dataclasses.UserInfo
import com.example.p2p_stock.errors.UserNotFoundException
import com.example.p2p_stock.services.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("\${application.info.apiLink}/user")
class UserController(
    private val userService: UserService,
) {
    // Новый endpoint для получения информации о пользователе на основе JWT токена
    @GetMapping("/profile")
    fun getUserProfile(@AuthenticationPrincipal userDetails: UserDetails): UserInfo {
        // Извлекаем username из объекта UserDetails (Spring Security подставляет его из токена)
        val username = userDetails.username

        // Получаем пользователя из базы данных по username
        val user = userService.findByEmail(username) ?: throw UserNotFoundException("Пользователь не найден")

        // Возвращаем информацию о пользователе
        return UserInfo(
            id = user.id,
            username = user.username,
            email = user.email,
            roleName = user.role!!.name
        )
    }
}
