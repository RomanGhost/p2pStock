package com.example.p2p_stock.controllers

import com.example.p2p_stock.errors.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<String> {
        // Возвращаем ответ с кодом 404 и сообщением
        return ResponseEntity(ex.message, HttpStatus.NOT_FOUND)
    }
}
