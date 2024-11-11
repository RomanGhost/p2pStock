package com.example.p2p_stock.initialize

import com.example.p2p_stock.models.OrderType
import com.example.p2p_stock.repositories.OrderTypeRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OrderTypeInitialize(val orderTypeRepository: OrderTypeRepository) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        val orderTypes = listOf(
            OrderType(name = "Покупка"),
            OrderType(name = "Продажа")
        )

        val existingOrderTypes = orderTypeRepository.findAll()

        for (orderType in orderTypes) {
            val exists = existingOrderTypes.any { it.name.equals(orderType.name, ignoreCase = true) }
            if (!exists) {
                orderTypeRepository.save(orderType)
            }
        }
    }
}
