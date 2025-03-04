package com.example.p2p_stock.controllers

import com.example.p2p_stock.configs.JWTUtil
import com.example.p2p_stock.configs.MyUserDetails
import com.example.p2p_stock.dataclasses.JwtToken
import com.example.p2p_stock.dataclasses.LoginUser
import com.example.p2p_stock.dataclasses.RegisterUser
import com.example.p2p_stock.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*


@CrossOrigin
@RestController
@RequestMapping("\${application.info.apiLink}/auth")
class UserAuthController (
    private val userService: UserService,
    private val jwtUtil: JWTUtil,
    private val authenticationManager: AuthenticationManager,
)
{
    @PostMapping("/register")
    fun registerUser(@RequestBody registerUser: RegisterUser): ResponseEntity<Any> {
        return try {
            // Регистрация пользователя через сервис
            val newUser = userService.register(registerUser)

            // Генерация JWT токена для нового пользователя
            val token = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(newUser.login, registerUser.password)
            ).principal.let { jwtUtil.generateToken(it as MyUserDetails) }

            // Возвращаем токен в ответе
            ResponseEntity.ok(JwtToken(token))
        } catch (e: IllegalArgumentException) {
            // Обработка ошибок, например, если email или username уже заняты
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error during registration: ${e.message}")
        } catch (e: Exception) {
            // Обработка других ошибок
            println("Какие-то ошибки: $e")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during registration: ${e.message}")
        }
    }

    @PostMapping("/login")
    fun loginUser(@RequestBody loginUser: LoginUser): ResponseEntity<Any> {
        return try {
            val getUser = userService.findByEmail(loginUser.email)
            if(getUser == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid credentials")
            }
            if(!getUser.isActive) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials")
            }
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(getUser.login, loginUser.password)
            )
            val token = jwtUtil.generateToken(authentication.principal as MyUserDetails)
            ResponseEntity.ok(JwtToken(token))
        } catch (e: BadCredentialsException) {
            // Возвращаем ошибку, если аутентификация не удалась
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials")
        }
    }

}