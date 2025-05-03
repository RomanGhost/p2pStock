package com.example.p2p_stock.scheduler

import com.example.p2p_stock.services.DealService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DealScheduler(
    private val dealService: DealService
) {

    @Scheduled(fixedRate = 60000) // каждые 60 секунд
    fun checkExpiredDeals() {
        dealService.closeExpiredDeals()
    }
}