package com.example.p2p_stock.services

import com.example.p2p_stock.models.Priority
import com.example.p2p_stock.repositories.PriorityRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class PriorityService(private val priorityRepository: PriorityRepository) {

    fun findAll(): List<Priority> = priorityRepository.findAll()

    fun findById(priorityName: String): Optional<Priority> = priorityRepository.findById(priorityName)

    fun save(priority: Priority): Priority = priorityRepository.save(priority)

    fun findByAmount(amount:Double): Priority {
        val priority = when (amount) {
            in 1_000_000.0..Double.MAX_VALUE -> findById("Критический")
            in 100_000.0..999_999.99 -> findById("Высокий")
            in 10_000.0..99_999.99 -> findById("Средний")
            else -> findById("Низкий")
        }

        return priority.get()

    }
}