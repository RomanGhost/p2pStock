package com.example.p2p_stock.services

import com.example.p2p_stock.models.DealStatus
import com.example.p2p_stock.repositories.DealStatusRepository
import org.springframework.stereotype.Service

@Service
class DealStatusService(private val dealStatusRepository: DealStatusRepository) {

    fun findAll(): List<DealStatus> = dealStatusRepository.findAll()

    fun save(status: DealStatus): DealStatus = dealStatusRepository.save(status)
}