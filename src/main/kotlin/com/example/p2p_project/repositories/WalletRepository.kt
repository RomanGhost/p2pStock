package com.example.p2p_project.repositories

import com.example.p2p_project.models.Wallet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WalletRepository : JpaRepository<Wallet, Long>{
    fun findByUserId(userId:Long):List<Wallet>
    fun existsByNameAndUserId(name:String, userId: Long):Boolean

    fun findByCryptocurrencyId(CryptocurrencyId:Long):List<Wallet>
}
