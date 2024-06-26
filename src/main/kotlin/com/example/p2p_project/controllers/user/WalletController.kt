package com.example.p2p_project.controllers.user

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.models.Wallet
import com.example.p2p_project.services.UserService
import com.example.p2p_project.services.WalletService
import com.example.p2p_project.services.dataServices.CryptocurrencyService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/platform/wallet")
class WalletController(
    private val walletService: WalletService,
    private val userService: UserService,
    private val cryptocurrencyService: CryptocurrencyService
) {

    @GetMapping("/add")
    fun getAddNewWallet(model: Model, redirectAttributes: RedirectAttributes) :String{
        val cryptocurrency = cryptocurrencyService.getAll()

        model.addAttribute("cryptocurrencies", cryptocurrency)
        model.addAttribute("wallet", Wallet())
        

        return "addWallet"
    }

    @PostMapping("/save")
    fun submitNewWallet(
        @ModelAttribute("wallet") wallet: Wallet,
        authentication: Authentication,
        redirectAttributes: RedirectAttributes
    ): String{
        if (wallet.name.length < 2){
            redirectAttributes.addFlashAttribute("errorMessage", "Wallet name is too short")
            return "redirect:/platform/wallet/add?error"
        }
        //TODO(Проверить есть ли такое название у пользователя в БД)
        if(walletService.existWalletForUserId(wallet.name, wallet.user.id)){
            redirectAttributes.addFlashAttribute("errorMessage", "Wallet name is exist")
            return "redirect:/platform/wallet/add?error"
        }

        val userDetails = authentication.principal as MyUserDetails
        wallet.user = userService.getById(userDetails.user.id)

        walletService.add(wallet)
        return "redirect:/platform/account/welcome"
    }

    @GetMapping("/delete/{walletId}")
    fun postDeleteWallet(@PathVariable walletId: Long): String {
        walletService.deleteById(walletId)
        return "redirect:/platform/account/welcome"
    }
}