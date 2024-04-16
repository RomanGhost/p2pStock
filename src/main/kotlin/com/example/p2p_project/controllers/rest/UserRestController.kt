package com.example.p2p_project.controllers.rest

import com.example.p2p_project.models.User
import com.example.p2p_project.models.dataTables.Role
import com.example.p2p_project.services.UserService
import com.example.p2p_project.services.dataServices.UserRoleService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("\${application.info.apiLink}/user")
class UserRestController(val userService: UserService,
                         val userRoleService: UserRoleService) {
    @PostMapping("/add")
    fun add(@RequestBody user: User): ResponseEntity<User> {
        val newUser = userService.add(user)
        userRoleService.addUserRole(newUser, "Пользователь")
        return ResponseEntity<User>(newUser, HttpStatus.CREATED)
    }

    @PostMapping("/add/admin")
    fun addAdmin(@RequestParam userId:Long){
        val user = userService.getById(userId)
        userRoleService.addUserRole(user, "Администратор")
    }


    @PutMapping("/update")
    fun update(
        @RequestBody user: User,
        @RequestParam(required = false) id:Long?,
        @RequestParam(required = false) login: String?
    ): ResponseEntity<Any> {

        val userId: Long =
            when {
                id != null -> id
                login != null -> userService.getByLogin(login)?.id
                else -> null
            } ?: return ResponseEntity("No parameters", HttpStatus.BAD_REQUEST)

        val updateUser = userService.update(user, userId)
        return ResponseEntity(updateUser, HttpStatus.OK)
    }

    @DeleteMapping("/delete/{userId}")
    fun delete(@PathVariable userId:Long): ResponseEntity<User> {
        userService.delete(userId)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/all")
    fun getAll(): ResponseEntity<List<User>> {
        val userList:List<User> = userService.getAll()
        return ResponseEntity(userList, HttpStatus.OK)
    }

    @GetMapping("/{userId}")
    fun getById(@PathVariable userId:Long): ResponseEntity<User> {
        val user = userService.getById(userId)
        return ResponseEntity(user, HttpStatus.OK)
    }

    @GetMapping("/get_user")
    fun getByParams(
        @RequestParam(required = false) login: String?
    ): ResponseEntity<Any> {
        val user: User =
            when {
                login != null -> userService.getByLogin(login)
                else -> null
            }?: return ResponseEntity("No parameters", HttpStatus.BAD_REQUEST)

            return ResponseEntity(user, HttpStatus.OK)
    }

    @GetMapping("/get_roles/{userId}")
    fun getRoles(@PathVariable userId:Long):ResponseEntity<List<Role>>{
        val roles = userService.getRoles(userId)
        return ResponseEntity(roles, HttpStatus.OK)
    }

}