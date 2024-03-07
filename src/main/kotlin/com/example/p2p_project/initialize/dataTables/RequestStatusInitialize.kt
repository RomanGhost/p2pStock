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

        requestStatuses.add(RequestStatus(name="Создание", id=7))
        requestStatuses.add(RequestStatus(name="Модерация", id=1))
        requestStatuses.add(RequestStatus(name="Отправлено на доработку", id=2))
        requestStatuses.add(RequestStatus(name="Ожидание на платформе", id=6))
        requestStatuses.add(RequestStatus(name="Используется в сделке", id=5))
        requestStatuses.add(RequestStatus(name="Закрыто: успешно", id=8))
        requestStatuses.add(RequestStatus(name="Закрыто: неактуально", id=4))
        requestStatuses.add(RequestStatus(name="Закрыто: проблема", id=3))

        for (requestStatus in requestStatuses) {
            requestStatusRepository.save(requestStatus)
        }
    }
}
