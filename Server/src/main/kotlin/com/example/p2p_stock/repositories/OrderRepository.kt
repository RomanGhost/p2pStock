package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByUserId(userId: Long): List<Order>
    fun findByWalletId(walletId: Long): List<Order>
}