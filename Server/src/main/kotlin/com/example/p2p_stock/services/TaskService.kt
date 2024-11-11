package com.example.p2p_stock.services

import com.example.p2p_stock.models.Task
import com.example.p2p_stock.repositories.TaskRepository
import org.springframework.stereotype.Service

@Service
class TaskService(private val taskRepository: TaskRepository) {

    fun findByDealId(dealId: Long): List<Task> = taskRepository.findByDealId(dealId)

    fun save(task: Task): Task = taskRepository.save(task)

    fun delete(id: Long) = taskRepository.deleteById(id)
}