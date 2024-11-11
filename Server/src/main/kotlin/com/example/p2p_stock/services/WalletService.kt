package com.example.p2p_stock.services

import com.example.p2p_stock.models.Wallet
import com.example.p2p_stock.repositories.WalletRepository
import org.springframework.stereotype.Service

@Service
class WalletService(private val walletRepository: WalletRepository) {

    fun findByUserId(userId: Long): List<Wallet> = walletRepository.findByUserId(userId)

    fun save(wallet: Wallet): Wallet = walletRepository.save(wallet)

    fun delete(id: Long) = walletRepository.deleteById(id)
}