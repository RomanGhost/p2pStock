package com.example.p2p_stock.services

import com.example.p2p_stock.models.Priority
import com.example.p2p_stock.repositories.PriorityRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class PriorityService(private val priorityRepository: PriorityRepository) {

    fun findAll(): List<Priority> = priorityRepository.findAll()

    fun findById(priorityName: String): Optional<Priority> = priorityRepository.findById(priorityName)

    fun save(priority: Priority): Priority = priorityRepository.save(priority)

    fun findByAmount(amount: BigDecimal): Priority {
        val critical = BigDecimal("1000000.00")
        val high = BigDecimal("100000.00")
        val medium = BigDecimal("10000.00")

        val priority = when {
            amount >= critical -> findById("Критический")
            amount >= high -> findById("Высокий")
            amount >= medium -> findById("Средний")
            else -> findById("Низкий")
        }

        return priority.get()
    }

}