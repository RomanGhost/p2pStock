package com.example.p2p_stock.services

import com.example.p2p_stock.models.Deal
import com.example.p2p_stock.repositories.DealRepository
import org.springframework.stereotype.Service

@Service
class DealService(private val dealRepository: DealRepository) {

    fun findByBuyOrderId(buyOrderId: Long): Deal? = dealRepository.findByBuyOrderId(buyOrderId).orElse(null)

    fun save(deal: Deal): Deal = dealRepository.save(deal)

    fun delete(id: Long) = dealRepository.deleteById(id)
}
