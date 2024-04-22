package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.RequestType
import com.example.p2p_project.repositories.dataTables.RequestTypeRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component

class RequestTypeInitialize(val requestTypeRepository: RequestTypeRepository): CommandLineRunner {
    @Transactional
    override fun run(vararg args: String?) {
        if (requestTypeRepository.count() != 0L) return;
        val requestTypes: MutableList<RequestType> = mutableListOf()

        requestTypes.add(RequestType(name = "Покупка"))
        requestTypes.add(RequestType(name = "Продажа"))

        for (requestType in requestTypes) {
            requestTypeRepository.save(requestType)
        }
    }
}