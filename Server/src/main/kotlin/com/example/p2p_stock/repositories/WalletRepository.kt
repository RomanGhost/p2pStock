package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.Wallet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.security.PublicKey

@Repository
interface WalletRepository : JpaRepository<Wallet, Long> {
    fun findByUserId(userId: Long): List<Wallet>
    fun findByPublicKey(publicKey: String):Wallet?
}