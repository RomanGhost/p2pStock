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

        dealStatuses.add(DealStatus(name="Подтверждение сделки", priority=3))
        dealStatuses.add(DealStatus(name="Ожидание перевода", priority=5))
        dealStatuses.add(DealStatus(name="Ожидание подтверждения перевода", priority=4))
        dealStatuses.add(DealStatus(name="Закрыто: успешно", priority=8))
        dealStatuses.add(DealStatus(name="Приостановлено: решение проблем", priority=2))
        dealStatuses.add(DealStatus(name="Ожидание решения менеджера", priority=1))
        dealStatuses.add(DealStatus(name="Закрыто: Время перевода истекло", priority=6))
        dealStatuses.add(DealStatus(name="Закрыто: неактуально", priority=7))

        for (dealStatus in dealStatuses) {
            dealStatusRepository.save(dealStatus)
        }
    }
}