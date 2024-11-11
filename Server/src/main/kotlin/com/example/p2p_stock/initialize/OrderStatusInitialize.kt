package com.example.p2p_stock.initialize

import com.example.p2p_stock.models.OrderStatus
import com.example.p2p_stock.repositories.OrderStatusRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OrderStatusInitialize(val requestStatusRepository: OrderStatusRepository) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        val requestStatuses = listOf(
            OrderStatus(name = "Модерация"),
            OrderStatus(name = "Отправлено на доработку"),
            OrderStatus(name = "Доступна на платформе"),
            OrderStatus(name = "Используется в сделке"),
            OrderStatus(name = "Закрыто: успешно"),
            OrderStatus(name = "Закрыто: неактуально"),
            OrderStatus(name = "Закрыто: проблема")
        )

        val existingRequestStatuses = requestStatusRepository.findAll()

        for (requestStatus in requestStatuses) {
            val exists = existingRequestStatuses.any { it.name.equals(requestStatus.name, ignoreCase = true) }
            if (!exists) {
                requestStatusRepository.save(requestStatus)
            }
        }
    }
}
