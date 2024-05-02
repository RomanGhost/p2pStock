package com.example.p2p_project.controllers.user

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.services.CardService
import com.example.p2p_project.services.DealService
import com.example.p2p_project.services.RequestService
import com.example.p2p_project.services.WalletService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/platform/account")

class UserAccountController(
    private val walletService: WalletService,
    private val cardService: CardService,
    private val requestService: RequestService,
    private val dealService: DealService
) {

    @GetMapping("/welcome")
    fun welcomePage(model: Model, authentication: Authentication):String{
        val userDetails = authentication.principal as MyUserDetails
        val login = userDetails.username
        val userId = userDetails.user.id

        val cards = cardService.getByUserId(userId)
        val wallets = walletService.getByUserId(userId)
        val requests = requestService.getByUserId(userId)
        val deals = dealService.getByUserId(userId)

        model.addAttribute("login", login)
        model.addAttribute("cards", cards)
        model.addAttribute("wallets", wallets)
        model.addAttribute("requests", requests)
        model.addAttribute("deals", deals)

        return "welcome"
    }

}