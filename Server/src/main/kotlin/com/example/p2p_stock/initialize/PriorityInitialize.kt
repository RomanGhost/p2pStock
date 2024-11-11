package com.example.p2p_stock.initialize

import com.example.p2p_stock.models.Priority
import com.example.p2p_stock.repositories.PriorityRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PriorityInitialize(val priorityRepository: PriorityRepository) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        val priorities = listOf(
            Priority(name = "Низкий", level = 3),
            Priority(name = "Средний", level = 2),
            Priority(name = "Высокий", level = 1),
            Priority(name = "Критический", level = 0),
        )

        val existingPriorities = priorityRepository.findAll()

        for (priority in priorities) {
            val exists = existingPriorities.any { it.name.equals(priority.name, ignoreCase = true) }
            if (!exists) {
                priorityRepository.save(priority)
            }
        }
    }
}
