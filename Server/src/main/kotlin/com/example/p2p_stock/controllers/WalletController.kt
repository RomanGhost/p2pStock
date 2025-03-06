package com.example.p2p_stock.controllers

import com.example.p2p_stock.dataclasses.WalletInfo
import com.example.p2p_stock.errors.UserException
import com.example.p2p_stock.services.CryptocurrencyService
import com.example.p2p_stock.services.UserService
import com.example.p2p_stock.services.WalletService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("\${application.info.apiLink}/wallet")
class WalletController(
    private val userService: UserService,
    private val walletService: WalletService,
    private val cryptocurrencyService: CryptocurrencyService
) {
    @GetMapping("/get_user_wallets")
    fun getUserWallets(@AuthenticationPrincipal userDetails: UserDetails,
                       @RequestParam(required = false) cryptocurrencyCode: String=""): List<WalletInfo> {
        // Извлекаем username из объекта UserDetails (Spring Security подставляет его из токена)
        val username = userDetails.username
//        println(cryptocurrencyCode)
        // Получаем пользователя из базы данных по email
        val user = userService.findByUsername(username) ?: throw UserException("Пользователь не найден")

        val crypto = if(cryptocurrencyCode != "") {
            cryptocurrencyService.findByCode(cryptocurrencyCode)
        } else {
            null
        }

        var wallets = walletService.findByUserId(user.id)
        if(crypto != null){
            wallets = wallets.filter { it.cryptocurrency?.code == crypto.code }
        }

        val walletsInfo = wallets.map {
            WalletInfo(
                id = it.id,
                walletName = it.name,
                balance = it.balance,
                cryptocurrencyCode = it.cryptocurrency!!.code
            )
        }
        return walletsInfo
    }



    @PostMapping("/add")
    fun addWallet4User(@AuthenticationPrincipal userDetails: UserDetails, @RequestBody walletInfo: WalletInfo): WalletInfo {
        val username = userDetails.username

        // Получаем пользователя из базы данных по email
        val user = userService.findByUsername(username) ?: throw UserException("Пользователь не найден")

        val newWallet = walletService.addWallet(walletInfo, user)
        return WalletInfo(
            id = newWallet.id,
            walletName = newWallet.name,
            balance = newWallet.balance,
            cryptocurrencyCode = newWallet.cryptocurrency!!.code
        )
    }

    @DeleteMapping("/delete")
    fun deleteWallet(@AuthenticationPrincipal userDetails: UserDetails, @RequestParam walletId:Long){
        val username = userDetails.username

        // Получаем пользователя из базы данных по email
        val user = userService.findByUsername(username) ?: throw UserException("Пользователь не найден")

        walletService.delete(walletId, user.id)
    }
}