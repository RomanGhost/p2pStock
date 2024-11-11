package com.example.p2p_stock.controllers

import com.example.p2p_stock.models.User
import com.example.p2p_stock.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("\${application.info.apiLink}/test")
class TestController (
    private val userService: UserService
){
    @PostMapping("/hello")
    fun test1(): ResponseEntity<String>{
        println("Запрос получен успешно!")
        return ResponseEntity("Hello from test", HttpStatus.OK)
    }


}