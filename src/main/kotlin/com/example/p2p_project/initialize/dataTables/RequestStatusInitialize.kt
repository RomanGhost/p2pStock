package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.RequestStatus
import com.example.p2p_project.repositories.dataTables.RequestStatusRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class RequestStatusInitialize(val requestStatusRepository: RequestStatusRepository): CommandLineRunner {
    override fun run(vararg args: String?) {
        if (requestStatusRepository.count() != 0L) return;
        val requestStatuses: MutableList<RequestStatus> = mutableListOf()

        requestStatuses.add(RequestStatus(null, "В ожидании", 3))
        requestStatuses.add(RequestStatus(null, "Закрыто: успешно", 2))
        requestStatuses.add(RequestStatus(null, "Закрыто: неактуально", 1))

        for (requestStatus in requestStatuses) {
            requestStatusRepository.save(requestStatus)
        }
    }
}
