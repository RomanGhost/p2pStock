package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.WalletCryptocurrencyBalance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WalletCryptocurrencyBalanceRepository : JpaRepository<WalletCryptocurrencyBalance, Long> {
    fun findByWalletId(walletId: Long): List<WalletCryptocurrencyBalance>
}