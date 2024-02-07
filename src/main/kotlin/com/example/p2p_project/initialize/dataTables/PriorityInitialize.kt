package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.Priority
import com.example.p2p_project.repositories.dataTables.PriorityRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class PriorityInitialize(val priorityRepository: PriorityRepository): CommandLineRunner {
    override fun run(vararg args: String?) {
        if (priorityRepository.count() != 0L) return;
        val priorities: MutableList<Priority> = mutableListOf()

        priorities.add(Priority(null, 5, "Низкий"))
        priorities.add(Priority(null, 4, "Средний"))
        priorities.add(Priority(null, 3, "Высокий"))

        for (priority in priorities) {
            priorityRepository.save(priority)
        }
    }
}