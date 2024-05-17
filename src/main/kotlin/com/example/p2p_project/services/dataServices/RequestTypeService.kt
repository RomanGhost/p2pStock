package com.example.p2p_project.services.dataServices

import com.example.p2p_project.models.dataTables.RequestType
import com.example.p2p_project.repositories.dataTables.RequestTypeRepository
import org.springframework.stereotype.Service


@Service
class RequestTypeService(private val requestTypeRepository: RequestTypeRepository) {
    fun getAll(): MutableList<RequestType> {
        return requestTypeRepository.findAll()
    }

    fun getByName(name:String):RequestType{
        return requestTypeRepository.findByName(name)
    }

}