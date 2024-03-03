package com.example.p2p_project.controllers

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.models.Card
import com.example.p2p_project.services.BankService
import com.example.p2p_project.services.CardService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("\${application.info.appLink}/card")
class CardController(
    private val cardService: CardService,
    private val bankService: BankService

) {
    @Value("\${application.info.appLink}")
    private lateinit var appLink: String
    @GetMapping("/add")
    fun getAddNewCard(model:Model, redirectAttributes: RedirectAttributes):String {
        val banks = bankService.getAll()
        model.addAttribute("banks", banks)
        model.addAttribute("card", Card())
        model.addAttribute("link", appLink)
        return "addCard"
    }

    @PostMapping("/submit")
    fun submitNewCard(
        @ModelAttribute("card") card: Card,
        authentication: Authentication,
        redirectAttributes: RedirectAttributes
    ):String {
        val userDetails = authentication.principal as MyUserDetails
        card.user = userDetails.user

        if (card.cardNumber.length < 16){
            redirectAttributes.addFlashAttribute("errorMessage", "Card number is too short")
            return "redirect:$appLink/card/add?error"
        }

        cardService.add(card)
        return "redirect:${appLink}/account/welcome"
    }
}