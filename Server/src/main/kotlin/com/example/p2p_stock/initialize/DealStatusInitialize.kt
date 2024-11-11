package com.example.p2p_stock.initialize

import com.example.p2p_stock.models.DealStatus
import com.example.p2p_stock.repositories.DealStatusRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DealStatusInitialize(val dealStatusRepository: DealStatusRepository) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        val dealStatuses = listOf(
            DealStatus(name = "Подтверждение сделки"),
            DealStatus(name = "Ожидание перевода"),
            DealStatus(name = "Ожидание подтверждения перевода"),
            DealStatus(name = "Закрыто: успешно"),
            DealStatus(name = "Приостановлено: решение проблем"),
            DealStatus(name = "Ожидание решения менеджера"),
            DealStatus(name = "Закрыто: время кс истекло"),
            DealStatus(name = "Закрыто: отменена менеджером"),
            DealStatus(name = "Закрыто: неактуально")
        )

        val existingDealStatuses = dealStatusRepository.findAll()

        for (dealStatus in dealStatuses) {
            val exists = existingDealStatuses.any { it.name.equals(dealStatus.name, ignoreCase = true) }
            if (!exists) {
                dealStatusRepository.save(dealStatus)
            }
        }
    }
}
