package com.example.p2p_project.controllers

import com.example.p2p_project.models.Card
import com.example.p2p_project.services.CardService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("\${application.info.api}/card")
class CardController(val cardService: CardService) {
    @PostMapping("/add")
    fun add(@RequestBody card: Card): ResponseEntity<Card> {
        val newCard = cardService.add(card)
        return ResponseEntity<Card>(newCard, HttpStatus.CREATED)
    }

    @PutMapping("/update")
    fun update(@RequestBody card: Card, @RequestParam id: Long): ResponseEntity<Card> {
        val updateCard = cardService.update(card, id)
        return ResponseEntity(updateCard, HttpStatus.OK)
    }

    @DeleteMapping("/delete/{cardId}")
    fun delete(@PathVariable cardId:Long): ResponseEntity<Card> {
        cardService.delete(cardId)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/all")
    fun getAll(): ResponseEntity<List<Card>> {
        val cardList:List<Card> = cardService.getAll()
        return ResponseEntity(cardList, HttpStatus.OK)
    }

    @GetMapping("/{cardId}")
    fun getById(@PathVariable cardId:Long): ResponseEntity<Card> {
        val card = cardService.getById(cardId)
        return ResponseEntity(card, HttpStatus.OK)
    }
}