package com.example.p2p_stock.services

import com.example.p2p_stock.models.Priority
import com.example.p2p_stock.repositories.PriorityRepository
import org.springframework.stereotype.Service

@Service
class PriorityService(private val priorityRepository: PriorityRepository) {

    fun findAll(): List<Priority> = priorityRepository.findAll()

    fun save(priority: Priority): Priority = priorityRepository.save(priority)
}