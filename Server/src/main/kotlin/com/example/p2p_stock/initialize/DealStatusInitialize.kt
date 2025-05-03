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
            DealStatus(name = "Подтверждение сделки", 10),
            DealStatus(name = "Ожидание перевода", 30),
            DealStatus(name = "Ожидание подтверждения перевода", 30),
            DealStatus(name = "Закрыто: успешно", -1),
            DealStatus(name = "Приостановлено: решение проблем", -1),
            DealStatus(name = "Ожидание решения менеджера", -1),
            DealStatus(name = "Закрыто: время кс истекло", -1),
            DealStatus(name = "Закрыто: отменена менеджером", -1),
            DealStatus(name = "Закрыто: неактуально", -1)
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
