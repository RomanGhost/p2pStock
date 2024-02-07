package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.RequestType
import com.example.p2p_project.repositories.dataTables.RequestTypeRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class RequestTypeInitialize(val requestTypeRepository: RequestTypeRepository): CommandLineRunner {
    override fun run(vararg args: String?) {
        if (requestTypeRepository.count() != 0L) return;
        val requestTypes: MutableList<RequestType> = mutableListOf()

        requestTypes.add(RequestType(null, "Покупка"))
        requestTypes.add(RequestType(null, "Продажа"))

        for (requestType in requestTypes) {
            requestTypeRepository.save(requestType)
        }
    }
}