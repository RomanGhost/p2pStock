package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.Card
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository : JpaRepository<Card, Long> {
    fun findByUserId(userId: Long): List<Card>
}