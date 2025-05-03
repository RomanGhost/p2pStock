package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.Deal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DealRepository : JpaRepository<Deal, Long>, JpaSpecificationExecutor<Deal> {
    fun findByBuyOrderId(buyOrderId: Long): Optional<Deal>
    fun findBySellOrderId(sellOrderId: Long): Optional<Deal>

    @Query("SELECT d.id FROM Deal d ORDER BY d.id DESC LIMIT 1")
    fun findLatestDeal(): Long?

    @Query(
        value = """
        SELECT * FROM deals d
        JOIN deal_statuses s ON d.status_id = s.name
        where s.timing_min  > 0 and 
        d.last_status_change < NOW() - (s.timing_min * INTERVAL '1 minute')
    """,
        nativeQuery = true
    )
    fun findExpiredDealsNative(): List<Deal>

}