package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.Deal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DealRepository : JpaRepository<Deal, Long>, JpaSpecificationExecutor<Deal> {
    fun findByBuyOrderId(buyOrderId: Long): Optional<Deal>
    fun findBySellOrderId(sellOrderId: Long): Optional<Deal>
}