package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.RequestStatus
import com.example.p2p_project.repositories.dataTables.RequestStatusRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RequestStatusInitialize(val requestStatusRepository: RequestStatusRepository): CommandLineRunner {
    @Transactional
    override fun run(vararg args: String?) {
        if (requestStatusRepository.count() != 0L) return;
        val requestStatuses: MutableList<RequestStatus> = mutableListOf()

        requestStatuses.add(RequestStatus(name="Модерация"))
        requestStatuses.add(RequestStatus(name="Отправлено на доработку"))
        requestStatuses.add(RequestStatus(name="Ожидание на платформе"))
        requestStatuses.add(RequestStatus(name="Используется в сделке"))
        requestStatuses.add(RequestStatus(name="Закрыто: успешно"))
        requestStatuses.add(RequestStatus(name="Закрыто: неактуально"))
        requestStatuses.add(RequestStatus(name="Закрыто: проблема"))

        for (requestStatus in requestStatuses) {
            requestStatusRepository.save(requestStatus)
        }
    }
}
