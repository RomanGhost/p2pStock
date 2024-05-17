package com.example.p2p_project.services.dataServices

import com.example.p2p_project.models.dataTables.RequestStatus
import com.example.p2p_project.repositories.dataTables.RequestStatusRepository
import org.springframework.stereotype.Service

@Service
class RequestStatusService(private val requestStatusRepository: RequestStatusRepository) {
    fun getAll(): MutableList<RequestStatus> {
        return requestStatusRepository.findAll()
    }

    fun getByName(name: String): RequestStatus {
        return requestStatusRepository.findByName(name)
    }

}