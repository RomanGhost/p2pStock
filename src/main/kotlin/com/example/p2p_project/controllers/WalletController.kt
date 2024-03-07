package com.example.p2p_project.controllers

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.models.Wallet
import com.example.p2p_project.services.CryptocurrencyService
import com.example.p2p_project.services.WalletService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/wallet")
class WalletController(
    private val walletService: WalletService,
    private val cryptocurrencyService: CryptocurrencyService
) {
 

    @GetMapping("/add")
    fun getAddNewWallet(model: Model, redirectAttributes: RedirectAttributes) :String{
        val cryptocurrency = cryptocurrencyService.getAll()

        model.addAttribute("cryptocurrency", cryptocurrency)
        model.addAttribute("wallet", Wallet())
        

        return "addWallet"
    }

    @PostMapping("/save")
    fun submitNewCard(
        @ModelAttribute("wallet") wallet: Wallet,
        authentication: Authentication,
        redirectAttributes: RedirectAttributes
    ): String{
        if (wallet.name.length < 2){
            redirectAttributes.addFlashAttribute("errorMessage", "Wallet name is too short")
            return "redirect:/wallet/add?error"
        }

        val userDetails = authentication.principal as MyUserDetails
        wallet.user = userDetails.user

        walletService.add(wallet)
        return "redirect:/account/welcome"
    }
}