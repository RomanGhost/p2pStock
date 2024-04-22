package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.Priority
import com.example.p2p_project.repositories.dataTables.PriorityRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component

class PriorityInitialize(val priorityRepository: PriorityRepository): CommandLineRunner {
    @Transactional
    override fun run(vararg args: String?) {
        if (priorityRepository.count() != 0L) return;
        val priorities: MutableList<Priority> = mutableListOf()

        priorities.add(Priority(-1, 5, "Низкий"))
        priorities.add(Priority(-1, 4, "Средний"))
        priorities.add(Priority(-1, 3, "Высокий"))

        for (priority in priorities) {
            priorityRepository.save(priority)
        }
    }
}