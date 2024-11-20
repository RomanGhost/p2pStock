package com.example.p2p_stock.controllers

import com.example.p2p_stock.dataclasses.CardInfo
import com.example.p2p_stock.errors.UserException
import com.example.p2p_stock.services.CardService
import com.example.p2p_stock.services.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("\${application.info.apiLink}/card")
class CardController(
    private val userService: UserService,
    private val cardService: CardService,
) {
    @GetMapping("/get_user_cards")
    fun getUserCards(@AuthenticationPrincipal userDetails: UserDetails): List<CardInfo> {
        // Извлекаем username из объекта UserDetails (Spring Security подставляет его из токена)
        val username = userDetails.username

        // Получаем пользователя из базы данных по username
        val user = userService.findByEmail(username) ?: throw UserException("Пользователь не найден")

        val cards = cardService.findByUserId(user.id)
        val cardsInfo = cards.map { CardInfo(
            id = it.id,
            cardName = it.cardName,
            cardNumber = it.cardNumber,
            bankName = it.bank?.name?:"",
        ) }
        return cardsInfo
    }

    @PostMapping("/add")
    fun addCard4User(@AuthenticationPrincipal userDetails: UserDetails, @RequestBody cardInfo: CardInfo): CardInfo {
        val username = userDetails.username

        // Получаем пользователя из базы данных по username
        val user = userService.findByEmail(username) ?: throw UserException("Пользователь не найден")

        val newCard = cardService.addCard(cardInfo, user)
        return CardInfo(
            id = newCard.id,
            cardName = newCard.cardName,
            cardNumber = newCard.cardNumber,
            bankName = newCard.bank?.name?:"",
        )
    }

    @DeleteMapping("/delete")
    fun deleteWallet(@AuthenticationPrincipal userDetails: UserDetails, @RequestParam cardId:Long){
        val username = userDetails.username

        // Получаем пользователя из базы данных по email
        val user = userService.findByEmail(username) ?: throw UserException("Пользователь не найден")

        cardService.delete(cardId, user.id)
    }
}