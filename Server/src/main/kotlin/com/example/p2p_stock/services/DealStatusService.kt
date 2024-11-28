package com.example.p2p_stock.services

import com.example.p2p_stock.models.DealStatus
import com.example.p2p_stock.repositories.DealStatusRepository
import org.springframework.stereotype.Service

@Service
class DealStatusService(private val dealStatusRepository: DealStatusRepository) {

    fun findById(statusName:String):DealStatus{
        return dealStatusRepository.findById(statusName).get()
    }

    fun findAll(): List<DealStatus> = dealStatusRepository.findAll()

    fun getFirstStatus(): DealStatus {
        return dealStatusRepository.findById("Подтверждение сделки").get()
    }

    fun save(status: DealStatus): DealStatus = dealStatusRepository.save(status)
}