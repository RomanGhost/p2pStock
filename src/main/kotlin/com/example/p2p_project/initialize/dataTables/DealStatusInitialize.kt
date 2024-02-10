package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.DealStatus
import com.example.p2p_project.repositories.dataTables.DealStatusRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component

class DealStatusInitialize(val dealStatusRepository: DealStatusRepository): CommandLineRunner {
    @Transactional
    override fun run(vararg args: String?) {
        if (dealStatusRepository.count() != 0L) return;
        val dealStatuses: MutableList<DealStatus> = mutableListOf()

        dealStatuses.add(DealStatus(null, "Ожидание перевода", 3))
        dealStatuses.add(DealStatus(null, "Ожидание подтверждения перевода", 2))
        dealStatuses.add(DealStatus(null, "Закрыто: успешно", 6))
        dealStatuses.add(DealStatus(null, "Ожидание решения менеджера", 1))
        dealStatuses.add(DealStatus(null, "Закрыто: Время перевода истекло", 4))
        dealStatuses.add(DealStatus(null, "Закрыто: неактуально", 5))

        for (dealStatus in dealStatuses) {
            dealStatusRepository.save(dealStatus)
        }
    }
}