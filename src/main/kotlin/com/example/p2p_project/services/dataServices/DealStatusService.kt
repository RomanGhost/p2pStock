package com.example.p2p_project.services.dataServices

import com.example.p2p_project.models.dataTables.DealStatus
import com.example.p2p_project.repositories.dataTables.DealStatusRepository
import org.springframework.stereotype.Service

@Service
class DealStatusService(private val dealStatusRepository: DealStatusRepository) {
    fun getByNameStatus(statusName:String): DealStatus {
        return dealStatusRepository.findByName(statusName)
    }

    fun getAll(): MutableList<DealStatus> {
        return dealStatusRepository.findAll()
    }
}