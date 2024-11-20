package com.example.p2p_stock.controllers

import com.example.p2p_stock.errors.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserException::class)
    fun handleUserNotFoundException(ex: UserException): ResponseEntity<String> {
        // Возвращаем ответ с кодом 404 и сообщением
        return ResponseEntity(ex.message, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(NotFoundOrderException::class)
    fun handleOrderNotFoundException(ex: NotFoundOrderException): ResponseEntity<String> {
        // Возвращаем ответ с кодом 404 и сообщением
        return ResponseEntity(ex.message, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(NotFoundWalletException::class)
    fun handleWalletNotFoundException(ex: NotFoundWalletException): ResponseEntity<String> {
        // Возвращаем ответ с кодом 404 и сообщением
        return ResponseEntity(ex.message, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(NotFoundCardException::class)
    fun handleCardNotFoundException(ex: NotFoundCardException): ResponseEntity<String> {
        // Возвращаем ответ с кодом 404 и сообщением
        return ResponseEntity(ex.message, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(DuplicateCardException::class)
    fun handleDuplicateCardException(ex: DuplicateCardException): ResponseEntity<String> {
        // Возвращаем ответ с кодом 404 и сообщением
        return ResponseEntity(ex.message, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(DuplicateWalletException::class)
    fun handleDuplicateWalletException(ex: DuplicateWalletException): ResponseEntity<String> {
        // Возвращаем ответ с кодом 404 и сообщением
        return ResponseEntity(ex.message, HttpStatus.CONFLICT)
    }
}
