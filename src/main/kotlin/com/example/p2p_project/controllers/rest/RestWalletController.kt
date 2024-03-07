package com.example.p2p_project.controllers.rest

import com.example.p2p_project.models.Wallet
import com.example.p2p_project.services.WalletService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("\${application.info.appLink}/wallet")
class RestWalletController(val walletService: WalletService) {
    @PostMapping("/add")
    fun add(@RequestBody wallet: Wallet): ResponseEntity<Wallet> {
        val newWallet = walletService.add(wallet)
        return ResponseEntity<Wallet>(newWallet, HttpStatus.CREATED)
    }

    @PutMapping("/update")
    fun update(@RequestBody wallet: Wallet, @RequestParam id:Long): ResponseEntity<Wallet> {
        val updateWallet = walletService.update(wallet, id)
        return ResponseEntity(updateWallet, HttpStatus.OK)
    }

    @DeleteMapping("/delete/{walletId}")
    fun delete(@PathVariable walletId:Long): ResponseEntity<Wallet> {
        walletService.delete(walletId)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/all")
    fun getAll(): ResponseEntity<List<Wallet>> {
        val walletList:List<Wallet> = walletService.getAll()
        return ResponseEntity(walletList, HttpStatus.OK)
    }

    @GetMapping("/{walletID}")
    fun getById(@PathVariable walletID:Long): ResponseEntity<Wallet> {
        val wallet: Wallet = walletService.getById(walletID)
        return ResponseEntity(wallet, HttpStatus.FOUND)
    }
}