package com.example.p2p_stock.controllers

import com.example.p2p_stock.dataclasses.UserForAdmin
import com.example.p2p_stock.dataclasses.UserInfo
import com.example.p2p_stock.errors.UserException
import com.example.p2p_stock.services.UserService
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

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
        val user = userService.findByUsername(username) ?: throw UserException("Пользователь не найден")

        // Возвращаем информацию о пользователе
        return userService.userToUserInfo(user)
    }

    @GetMapping("/admin/get_all")
    fun getAllUsers(
        @PageableDefault(sort = ["id"], direction = Sort.Direction.ASC) pageable: Pageable,
        @RequestParam(required = false) id: Long?,
        @RequestParam(required = false) isActive: Boolean?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) updatedAfter: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) updatedBefore: LocalDateTime?,
        @AuthenticationPrincipal userDetails: UserDetails
    ): PagedModel<UserForAdmin> {
        val adminEmail = userDetails.username

        val users = userService.findByFilters(
            id = id,
            isActive = isActive,
            updatedAfter = updatedAfter,
            updatedBefore = updatedBefore,
            adminEmail = adminEmail,
            pageable = pageable
        )
            .map { userService.userToUserForAdmin(it) }
//        users.pageable.pageSize
        return PagedModel(users)
    }

    @PatchMapping("/admin/block")
    fun patchBlockUser(@RequestParam userId:Long): UserForAdmin{
        val user = userService.findById(userId) ?: throw UserException("User with id $userId not found ")
        val blockUser = userService.blockUser(user)
        return userService.userToUserForAdmin(blockUser)
    }

    @PatchMapping("/admin/unblock")
    fun patchUnblockUser(@RequestParam userId:Long): UserForAdmin{
        val user = userService.findById(userId) ?: throw UserException("User with id $userId not found ")
        val unblockUser = userService.unblockUser(user)
        return userService.userToUserForAdmin(unblockUser)
    }
}
