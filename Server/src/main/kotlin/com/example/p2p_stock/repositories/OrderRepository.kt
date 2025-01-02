package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
//    fun findByUserId(userId: Long): List<Order>
    fun findByWalletId(walletId: Long): List<Order>
    @Query("SELECT o.id FROM Order o ORDER BY o.id DESC LIMIT 1")
    fun findLatestOrder(): Long?
}